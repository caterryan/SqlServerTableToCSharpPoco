import com.intellij.database.model.DasTable
import com.intellij.database.util.Case
import com.intellij.database.util.DasUtil

/*
 * Available context bindings:
 *   SELECTION   Iterable<DasObject>
 *   PROJECT     project
 *   FILES       files helper
 */

packageName = ""
typeMapping = [
  (~/(?i)bigint/)                                 : "long",
  (~/(?i)binary|varbinary|timestamp/)             : "byte[]",
  (~/(?i)bit/)                                    : "bool",
  (~/(?i)char|nchar|varchar|nvarchar/)            : "string",
  (~/(?i)date|datetime|datetime2|smalldatetime/)  : "DateTime",
  (~/(?i)datetime/)                               : "DateTimeOffset",
  (~/(?i)decimal|numeric|money|smallmoney/)       : "decimal",
  (~/(?i)float/)                                  : "double",
  (~/(?i)int/)                                    : "int",
  (~/(?i)real/)                                   : "float",
  (~/(?i)smallint/)                               : "short",
  (~/(?i)time/)                                   : "TimeSpan",
  (~/(?i)tinyint/)                                : "byte",
]

FILES.chooseDirectoryAndSave("Choose directory", "Choose where to store generated files") { dir ->
    SELECTION.filter { it instanceof DasTable && it.getKind() == ObjectKind.TABLE }.each { generate(it, dir) }
}

def generate(table, dir) {
    def className = pascalCase(table.getName())
    def fields = calcFields(table)
    new File(dir, className + ".cs").withPrintWriter { out -> generate(out, className, fields, table) }
}

def generate(out, className, fields, table) {
  out.println "using System.ComponentModel.DataAnnotations.Schema;"
  out.println "using JetBrains.Annotations;"
  out.println ""
  out.println "namespace $packageName;"
  out.println ""
  out.println "[PublicAPI]"
  out.println "[Table(\"${table.getName()}\")]"
  out.println "public class $className"
  out.println "{"
  fields.each() {
    if (it.primarykey) {
      out.println "    [Key]"
    }
    if (it.comment != "") {
      out.println ""
      out.println "    //${it.comment}"
    }
    if (it.annos != "") out.println "  ${it.annos}"
    def nullOp = "?"
    if (it.notNull) nullOp = ""
    out.println "    public ${it.type}${nullOp} ${it.name.capitalize()} {get; set;}"
  }
  out.println "}"
}

def calcFields(table, out) {
  DasUtil.getColumns(table).reduce([]) { fields, col ->
    def spec = Case.LOWER.apply(col.getDataType().getSpecification())
    def typeStr = typeMapping.find { p, t -> p.matcher(spec).find() }.value

    fields += [[
                 name : javaName(col.getName(), false),
                 type : typeStr,
                 notNull: col.notNull,
                 annos: ""]]
  }
}

def javaName(str, capitalize) {
  def s = com.intellij.psi.codeStyle.NameUtil.splitNameIntoWords(str)
    .collect { Case.LOWER.apply(it).capitalize() }
    .join("")
    .replaceAll(/[^\p{javaJavaIdentifierPart}[_]]/, "_")
  capitalize || s.length() == 1 ? s : Case.LOWER.apply(s[0]) + s[1..-1]
}
