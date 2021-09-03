import modules.DbManager;

import java.sql.Statement;

public class DbTest {
    public static void main(String args[]) {
        DbManager db_conn = new DbManager("jdbc:mysql://localhost/alpelisib?serverTimezone=UTC", "root", "1541");
        Statement st = db_conn.connect_db();

    }

}
