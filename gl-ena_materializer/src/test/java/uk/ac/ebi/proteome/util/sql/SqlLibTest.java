package uk.ac.ebi.proteome.util.sql;

import junit.framework.TestCase;

public class SqlLibTest extends TestCase {

    public void testSqlLib() {
        SqlLib lib = new SqlLib("/uk/ac/ebi/proteome/util/sql/test_sql.xml");
        assertTrue(lib.getQuery("one").contains("some sql"));
        assertTrue(lib.getQuery("two").contains("{0}"));
        String q = lib.getQuery("two",new String[]{"bob","terry"});
        assertTrue(q.contains("bob"));
        assertTrue(q.contains("terry"));
        assertTrue(!q.contains("{0}"));
        assertTrue(!q.contains("{1}"));
        String q2 = lib.getQuery("three",new String[]{"bob"});
        assertTrue(q2.contains("bob"));
        assertTrue(!q.contains("{0}"));
    }
    
}
