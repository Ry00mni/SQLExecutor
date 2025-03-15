package app1;

public class UpdateDB {
	public static void main(String args[]) {
		/* DB接続情報 */
	    final String URL = "jdbc:mysql://localhost/shop";
		final String USER = "root";
		final String PASS = "";
		
		/* 実行用SQL */
		final String sql = "START TRANSACTION;\n"
				+ "\n"
				+ "INSERT INTO Shohin VALUES ('0001', 'Tシャツ' ,'衣服', 1000, 500, '2009-09-20');\n"
				+ "INSERT INTO Shohin VALUES ('0002', '穴あけパンチ', '事務用品', 500, 320, '2009-09-11');\n"
				+ "INSERT INTO Shohin VALUES ('0003', 'カッターシャツ', '衣服', 4000, 2800, NULL);\n"
				+ "INSERT INTO Shohin VALUES ('0004', '包丁', 'キッチン用品', 3000, 2800, '2009-09-20');\n"
				+ "INSERT INTO Shohin VALUES ('0005', '圧力鍋', 'キッチン用品', 6800, 5000, '2009-01-15');\n"
				+ "INSERT INTO Shohin VALUES ('0006', 'フォーク', 'キッチン用品', 500, NULL, '2009-09-20');\n"
				+ "INSERT INTO Shohin VALUES ('0007', 'おろしがね', 'キッチン用品', 880, 790, '2008-04-28');\n"
				+ "INSERT INTO Shohin VALUES ('0008', 'ボールペン', '事務用品', 100, NULL, '2009-11-11');\n"
				+ "\n"
				+ "COMMIT;";
		
		/* Data Access Objectの生成 */
		DAO dao = new DAO(URL, USER, PASS);
		
		/* SQL分の解析、実行*/
		dao.validationSQL(sql);
	}
	
	
	
	
		

}