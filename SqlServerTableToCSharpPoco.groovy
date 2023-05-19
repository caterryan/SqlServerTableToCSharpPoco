import com.intellij.database.model.DasTable
import com.intellij.database.model.ObjectKind
import com.intellij.database.util.Case
import com.intellij.database.util.DasUtil

packageName = ""
typeMapping = [
  (~/(?i)bigint/)                                 : "long",
  (~/(?i)smallint/)                               : "short",
  (~/(?i)\bint/)                                    : "int",
  (~/(?i)float/)                                  : "double",
  (~/(?i)real/)                                   : "float",
  (~/(?i)decimal|numeric|money|smallmoney/)       : "decimal",
  (~/(?i)binary|varbinary|timestamp/)             : "byte[]",
  (~/(?i)tinyint/)                                : "byte",
  (~/(?i)bit/)                                    : "bool",
  (~/(?i)char|nchar|varchar|nvarchar/)            : "string",
  (~/(?i)date|datetime|datetime2|smalldatetime/)  : "DateTime",
  (~/(?i)datetimeoffset/)                         : "DateTimeOffset",
  (~/(?i)time/)                                   : "TimeSpan",
]

notNullableTypes = [  ]

FILES.chooseDirectoryAndSave("Choose directory", "Choose where to store generated files") { dir ->
    SELECTION.filter { it instanceof DasTable && it.getKind() == ObjectKind.TABLE }.each { generate(it, dir) }
}

def generate(table, dir) {
    def className = pascalCase(table.getName())
    def fields = calcFields(table)
    new File(dir, className + ".cs").withPrintWriter { out -> generate(out, className, fields, table) }
}

def generate(out, className, fields, table) {
    out.println "using System.ComponentModel.DataAnnotations;"
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
        
        if (it.primarykey)
        {
            out.println "    [Key]"
        }

        if (it.comment != "")
        {
            out.println "";
            out.println "    //${it.comment}";
        }

        out.println "    public ${it.type} ${it.name} { get; set; }"
    }
    out.println "}"
}

def calcFields(table) {
    DasUtil.getColumns(table).reduce([]) { fields, col ->
        def spec = Case.LOWER.apply(col.getDataType().getSpecification())
        def isArray = spec.contains('[]')
        def typeStr = typeMapping.find { p, t -> p.matcher(spec.replace("[]", "")).find() }?.value ?: "string"
        
        if (isArray) 
        {
            typeStr = "List<${typeStr}>"
        }
        
        def nullable = col.isNotNull() || typeStr in notNullableTypes ? "" : "?"
        def pk = DasUtil.getPrimaryKey(table).toString();

        fields += [[
                           primarykey : pk != null && pk != "" && pk.contains("(${col.getName()})") ? true : false,
                           colname : col.getName(),
                           spec : spec,
                           name : pascalCase(col.getName()),
                           type : typeStr + nullable,
                           comment : col.comment ? col.comment : ""]]
    }
}

def pascalCase(str) {
    com.intellij.psi.codeStyle.NameUtil.splitNameIntoWords(str)
            .collect { Case.LOWER.apply(it).capitalize() }
            .join("")
}