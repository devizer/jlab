package org.universe.sql;

import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.io.IOUtils;
import org.universe.System6;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * We need know DB structure before ORM stuff
 * @author: vladimir goyda
 */
public class ConnectionMetaDataReader {

    public TableCollection Tables;

    // 'SQLite' Ver '3.7.2' | 'MySQL' Ver '5.5.32-0ubuntu0.13.04.1'
    public String DbShortDescription;

    // 'MySQL Connector Java' Ver 'mysql-connector-java-5.1.26 ( Revision: ${bzr.revision-id} )'
    // 'SQLiteJDBC' Ver 'native'
    public String DriverShortDescription;

    // 4 Lines
    public String Description;

    // MySQL | SQLite
    public String DatabaseProductName;

    public static class TableCollection extends ArrayList<Table>
    {
        public TableCollection() {
        }

        public Table getTable(String name)
        {
            for(Table t:this)
                if (String.CASE_INSENSITIVE_ORDER.compare(t.TableName, name) == 0)
                    return t;

            return null;
        }
    }

    public static class Table extends ArrayList<Column>
    {
        public String TableName;
        public List<String> PrimaryKeyColumns;
        public List<Index> Indexes;
        public List<Reference> ForeignKeys;
        // Eighter directly or indirectly
        public List<String> ReferencedTables;

        public Column getColumn(String name)
        {
            for(Column c:this)
                if (String.CASE_INSENSITIVE_ORDER.compare(c.Name, name) == 0)
                    return c;

            return null;
        }
    }

    public static class Reference
    {
        public String ForeignKeyName;
        public String OtherTableName;
        public List<String> Columns;
        public List<String> OtherTableColumns;

        public Reference() {
        }
    }

    public static class Column
    {
        public String TableName;
        public String Name;
        public String Type;
        public Integer Size;
        // public String FullType;
        public boolean IsNullable;

        public Column() {
        }
    }

    public static class Index
    {
        public String IndexName;
        public List<String> Columns;
        public boolean IsUnique;
    }

    public static ConnectionMetaDataReader Build(Connection con) throws SQLException
    {
        ConnectionMetaDataReader ret = new ConnectionMetaDataReader();
        ret.BuildImpl(con);
        return ret;
    }

    private void BuildImpl(Connection con) throws SQLException
    {
        ConnectionMetaDataReader ret = this;
        ret.LogBuilder = new StringBuilder();
        ret.StartAt = System.nanoTime();
        DatabaseMetaData md = con.getMetaData();
        String pName = md.getDatabaseProductName();
        String pVer = md.getDatabaseProductVersion();
        ret.DbShortDescription = String.format("%s Ver %s", pName, pVer);
        ret.DatabaseProductName = pName;
        int jdbcMajor = md.getJDBCMajorVersion();
        int jdbcMinor = md.getJDBCMajorVersion();

        String drn = md.getDriverName();
        String drv = md.getDriverVersion();
        // md.getSQLStateType()
        ret.DriverShortDescription = String.format("%s Ver %s", drn, drv);

        String url = md.getURL();
        String xx = md.getIdentifierQuoteString();
        Description = String.format(
                "  DB-Product '%s' Ver '%s'\r\n  JDBC '%d.%d'\r\n  Driver '%s' Ver '%s'\r\n  Url: %s", new Object[]{
                pName, pVer,
                jdbcMajor, jdbcMinor,
                drn,
                drv,
                url
        });

        AppendLog("Connection class is '" + con.getClass() + "'");


        // DumpResultSet("Catalogs", md.getCatalogs());
        // DumpResultSet("Tables", md.getTables(null, null, "", new String[]{"TABLE"}));

        // 1: CATALOGS
        DumpResultSet(
                "Catalogs:",
                "getCatalogs()",
                md.getCatalogs());

        List<String> catalogs = readResultSetAsStrings(md.getCatalogs(), "TABLE_CAT");
        AppendLog(String.format("So, Catalogs are: %s", catalogs));

        // 2: TABLES
        String catalog = con.getCatalog();
        AppendLog("Catalog is '" + catalog + "'");

        DumpResultSet(
                "Tables:",
                "getTables()",
                md.getTables(catalog, null, "%", new String[]{"TABLE"}));

        List<String> tableNameList = readResultSetAsStrings(md.getTables(catalog, null, "%", new String[]{"TABLE"}), "TABLE_NAME");
        AppendLog(String.format("So, Tables are: %s", tableNameList));

        ret.Tables = new TableCollection();
        for(String tableName : tableNameList)
        {
            final Table table = new Table();
            table.TableName = tableName;
            ret.Tables.add(table);

            fetchColumns(md, table);
            fetchPrimaryKey(md, table);
            fetchIndexes(md, table);
            fetchForeignKeys(md, table);
        }

        for(Table t : ret.Tables)
        {
            ArrayList<String> refs = new ArrayList<String>();
            EnumRefs(t, refs);
            t.ReferencedTables = refs;
        }


/*
        // SQLite fails:AbstractMethodError
        DumpResultSet(
                "Client info properties:",
                "getClientInfoProperties()",
                md.getClientInfoProperties());
*/



        // Auchtougnht!
        // All Tables mode (%,%) of .getColumns retursn only 1 table (sqlite) :(
        // DumpResultSet("Columns:", md.getColumns(null, null, "%", "%" ));
    }

    void EnumRefs(Table table, List<String> names)
    {
        for(Reference r : table.ForeignKeys)
        {
            Table other = Tables.getTable(r.OtherTableName);
            if (other == null)
                continue;

            boolean visited = names.contains(other.TableName);
            if (visited)
                continue;

            names.add(other.TableName);
            EnumRefs(other, names);
        }
    }





    // COLUMNS!
    private void fetchColumns(DatabaseMetaData md, Table table) throws SQLException {
        DumpResultSet(
                "Columns OF <" + table.TableName + ">:",
                "getColumns(" + table.TableName + ")",
                md.getColumns(null, null, table.TableName, "%" ));

        ResultSetHandler<List<Column>> handlerColumns = new ResultSetHandler<List<Column>>() {
            @Override
            public List<Column> handle(ResultSet rs) throws SQLException {
                ArrayList<Column> cols = new ArrayList<Column>();
                while(rs.next())
                {
                    Column i = new Column();
                    cols.add(i);
                    i.Name = rs.getString("COLUMN_NAME");
                    i.TableName = rs.getString("TABLE_NAME");
                    i.Type = rs.getString("TYPE_NAME");
                    i.Size = rs.getInt("COLUMN_SIZE");
                    i.IsNullable = convertToBoolean(rs.getObject("NULLABLE"));
                }
                cols.trimToSize();
                return cols;
            }};

        List<Column> columns = handlerColumns.handle(md.getColumns(null, null, table.TableName, "%"));
        table.addAll(columns);
    }

    // 4. PRIMARY KEYS!
    private void fetchPrimaryKey(DatabaseMetaData md, Table table) throws SQLException {
        DumpResultSet(
                "PRIMARY KEYS OF <" + table.TableName + ">:",
                "getPrimaryKeys(" + table.TableName + ")",
                md.getPrimaryKeys(null, null, table.TableName));

        table.PrimaryKeyColumns = readResultSetAsStrings(
                md.getPrimaryKeys(null, null, table.TableName), "COLUMN_NAME");
    }

    private void fetchForeignKeys(DatabaseMetaData md, final Table table) throws SQLException {
/*
        // SQLite fails with
        // [SQLITE_ERROR] SQL error or missing database (near ")": syntax error)
        DumpResultSet(
            "Exported Keys of <" + tableName + "> (refs TO the table):",
            "getExportedKeys(" + tableName + ")",
            md.getExportedKeys(null, null, tableName));
*/

        // returns references FROM the table
        // Both sqlite & mysql supports:
        // 3  PKTABLE_NAME-text:'Category'
        // 4  PKCOLUMN_NAME-text:'Id'
        // 8  FKCOLUMN_NAME-text:'IdCategory'
        // 9  KEY_SEQ-integer:'1'
        // mysql only:
        // 12  FK_NAME-UNKNOWN:'FK_Product_Category'
        // KEY_SEQ == 1 means next foreign key
        DumpResultSet(
                "Imported Keys of <" + table.TableName + "> (refs FROM the table):",
                "getImportedKeys(" + table.TableName + ")",
                md.getImportedKeys(null, null, table.TableName));

        ResultSetHandler<List<Reference>> handlerFKs = new ResultSetHandler<List<Reference>>() {
            @Override
            public List<Reference> handle(ResultSet rs) throws SQLException {
                ArrayList<Reference> fkList = new ArrayList<Reference>();
                Reference prev = null;
                int prevKeySeq = 1;
                while(rs.next())
                {
                    String pkColumnName = rs.getString("PKCOLUMN_NAME");
                    String pkTableName = rs.getString("PKTABLE_NAME");
                    String thisColumnName = rs.getString("FKCOLUMN_NAME");
                    String fkName = rs.getString("FK_NAME");
                    int keySeq = rs.getInt("KEY_SEQ");
                    if (prev == null || keySeq <= prevKeySeq)
                    {
                        prev = new Reference();
                        fkList.add(prev);
                        prev.OtherTableName = pkTableName;
                        prev.Columns = new ArrayList<String>();
                        prev.OtherTableColumns = new ArrayList<String>();
                        if (fkName == null || fkName.length() == 0) // sqlite!
                            fkName = "~FK_" + table.TableName + "_" + prev.OtherTableName + "_" + fkList.size();

                        prev.ForeignKeyName = fkName;
                    }
                    prev.Columns.add(thisColumnName);
                    prev.OtherTableColumns.add(pkColumnName);
                }


                fkList.trimToSize();
                return fkList;
            }};

        table.ForeignKeys = handlerFKs.handle(md.getImportedKeys(null, null, table.TableName));
    }

    // INDEXES!
    private void fetchIndexes(DatabaseMetaData md, Table table) throws SQLException {
        // NON_UNIQUE: 0|1 - sqlite, true|false: mysql
        // COLUMN_NAME, ORDINAL_POSITION, INDEX_NAME, NON_UNIQUE: OK
        // ASC_OR_DESC - doesn't matter
        // Both PK & FK are included!
        DumpResultSet(
                "ALL INDEXES OF <" + table.TableName + "> (including PKs and FKs):",
                "getIndexInfo(" + table.TableName + ")",
                md.getIndexInfo(null, null, table.TableName, false, false));

        ResultSetHandler<List<Index>> handlerIndexes = new ResultSetHandler<List<Index>>() {
            @Override
            public List<Index> handle(ResultSet rs) throws SQLException {
                ArrayList<Index> ixs = new ArrayList<Index>();
                Index prev = null;
                while(rs.next())
                {
                    String columnName = rs.getString("COLUMN_NAME");
                    String indexName = rs.getString("INDEX_NAME");
                    // клиент mssql первую строчку любит ни о чем возвращать
                    if (columnName == null || indexName == null) continue;
                    boolean nonUnique = convertToBoolean(rs.getObject("NON_UNIQUE"));
                    if (prev == null || !prev.IndexName.equals(indexName))
                    {
                        prev = new Index();
                        prev.IndexName = indexName;
                        prev.Columns = new ArrayList<String>();
                        prev.Columns.add(columnName);
                        prev.IsUnique = !nonUnique;
                        ixs.add(prev);
                    }
                    else
                        prev.Columns.add(columnName);
                }

                ixs.trimToSize();
                return ixs;
            }};

        table.Indexes = handlerIndexes.handle(md.getIndexInfo(null, null, table.TableName, false, false));
        Collections.sort(table.Indexes, INDEX_COMPARATOR);
    }

    public String getInternalLog()
    {
        return LogBuilder.toString();
    }

    public void DumpInternalLog(boolean includeSchema, String fileName) throws IOException {
        OutputStream o = null;
        try
        {
            o = new FileOutputStream(fileName);
            if (includeSchema) IOUtils.write(this.toString() + System6.lineSeparator(), o);
            IOUtils.write(LogBuilder.toString(), o);
        }
        finally {
            if (o != null) o.close();
        }
    }

    @Override
    public String toString()
    {
        final StringBuilder ret = new StringBuilder();
        final String ls = System6.lineSeparator();
        ret.append(String.format("/*%s%s%s*/%s", ls, this.Description, ls, ls));
        for(Table t : Tables) {
            ret.append(ls);

            // Primary Key
            String pk = asString(t.PrimaryKeyColumns);

            // Columns
            List<String> rows = new ArrayList<String>();
            for(Column column : t)
            {
                boolean needSize =
                        column.Type != null
                        && (column.Type.toLowerCase().contains("char")
                        || column.Type.toLowerCase().contains("binary"));

                boolean isSqlite = this.DatabaseProductName != null && this.DatabaseProductName.toLowerCase().contains("sqlite");

                rows.add(String.format(
                        "%s %s%s%s",
                        column.Name,
                        column.Type,
                        column.Size > 0 && needSize && !isSqlite ? "(" + column.Size + ")" : "",
                        column.IsNullable ? "" : " Not Null"));
            }

            // Primary Key
            if (pk.length() > 0)
                rows.add("Constraint PK_" + t.TableName + " PRIMARY KEY On (" + pk + ")");

            // Foreign Keys
            for(Reference ref : t.ForeignKeys)
            {
                rows.add(String.format(
                        "Constraint %s Foreign Key (%s) REFERENCES %s(%s)",
                        ref.ForeignKeyName,
                        asString(ref.Columns),
                        ref.OtherTableName,
                        asString(ref.OtherTableColumns)));
            }

            //Foreign Keys Digest;
            if (t.ReferencedTables.size() > 0)
                rows.add(String.format("/* Dependencies: %s */", asString(t.ReferencedTables)));

            ret.append("CREATE TABLE ").append(t.TableName).append(" (")
                    .append(rows.size() == 0 ? " )" : "")
                    .append(ls);

            for(int i=0; i<rows.size(); i++)
                ret.append("  ").append(rows.get(i))
                        .append(i == rows.size() - 1 ? ls + ");" : ",")
                        .append(ls);

            // Indexes
            for(Index index : t.Indexes)
            {
                ret.append(String.format("Create %sIndex %s On %s(%s);%s",
                        index.IsUnique ? "UNIQUE " : "",
                        index.IndexName,
                        t.TableName,
                        asString(index.Columns),
                        ls));
            }

        }
        return ret.toString();
    }

    static String asString(List list)
    {
        StringBuilder ret = new StringBuilder();
        for(Object i : list)
            ret.append(ret.length() == 0 ? "" : ", ").append(i.toString());

        return ret.toString();
    }

    static boolean convertToBoolean(Object value)
    {
        if (value == null || value instanceof Void) return false;
        String s = value.toString().toLowerCase();
        return s.equals("1")
                || s.equals("true")
                || s.equals("yes");
    }

    static List<String> readResultSetAsStrings(ResultSet rs, String columnName) throws SQLException {
        ArrayList<String> ret = new ArrayList<String>();
        while(rs.next())
            ret.add(rs.getString(columnName));

        ret.trimToSize();
        return ret;
    }

    private void DumpResultSet(String titleSet, String titleRow, ResultSet rs) throws SQLException {
        StringBuilder sb = new StringBuilder();
        ResultSetMetaData md = rs.getMetaData();

        int n=0;
        while(rs.next())
        {
            sb.append("#" + (++n) + " row " + titleRow + System6.lineSeparator());
            for(int i=1; i<=md.getColumnCount(); i++)
                sb.append("  " + i + "  " + md.getColumnName(i) + "-" + md.getColumnTypeName(i) + ":'" + rs.getObject(i) + "'\r\n");
        }
        rs.close();
        AppendLog(String.format("Metadata of " + titleSet + System6.lineSeparator() + sb.toString()));
    }

    // IMPLEMENTATION
    private StringBuilder LogBuilder = new StringBuilder();
    private long StartAt;

    void AppendLog(String msg)
    {
        long msec = (System.nanoTime() - StartAt) / 1000000L;
        LogBuilder.append(String.format("%-8d %s", msec, msg)).append(System6.lineSeparator());
    }

    // obsolete
    static List<Map<String,Object>> readResultSet(ResultSet rs) throws SQLException {
        ResultSetMetaData md = rs.getMetaData();
        List<Map<String,Object>> ret = new ArrayList<Map<String, Object>>();
        while(rs.next())
        {
            HashMap<String,Object> item = new HashMap<String, Object>();
            for(int i=1; i<=md.getColumnCount(); i++)
                item.put(md.getColumnName(i), rs.getObject(i));
            ret.add(item);
        }

        rs.close();
        return ret;
    }

    // Sort indexes: !IsUnique, Name
    static final Comparator<Index> INDEX_COMPARATOR = new Comparator<Index>() {
        @Override
        public int compare(Index o1, Index o2) {
            int r1 = (o1.IsUnique ? 0 : 1) -(o2.IsUnique ? 0 : 1);
            if (r1 != 0) return r1;
            return String.CASE_INSENSITIVE_ORDER.compare(o1.IndexName, o2.IndexName);
        }
    };
}
