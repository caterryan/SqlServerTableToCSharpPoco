create table leadgen.table_name
(
    bigint_long                   bigint,
    smallint_short                smallint not null,
    int_int                       int,
    float_double                  float not null,
    real_float                    real,
    decimal_decimal               decimal(48, 2) not null,
    numeric_decimal               numeric,
    money_decimal                 money not null,
    smallmoney_decimal            smallmoney,
    binary_bytearray              binary not null,
    varbinary_bytearray           varbinary(max),
    timestamp_bytearray           timestamp not null,
    tinyint_byte                  tinyint ,
    bit_boolean                   bit not null,
    char_string                   char,
    nchar_string                  nchar(max) not null,
    varchar_string                varchar(max),
    nvarchar_string               nvarchar(32) not null,
    date_datetime                 date,
    datetime_datetime             datetime not null,
    datetime2_datetime            datetime2,
    smalldatetime_datetime        smalldatetime not null,
    datetimeoffset_datetimeoffset datetimeoffset,
    time_timespan                 time not null
    );

