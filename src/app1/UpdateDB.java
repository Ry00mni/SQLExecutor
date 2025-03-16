package app1;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;

public class UpdateDB {
	public static void main(String args[]) {
		
		/* DB接続情報 */
	    final String URL = "jdbc:mysql://localhost/Shop";
		final String USER = "root";
		final String PASS = "";
		
		/* Data Access Objectの生成 */
		DAO dao = new DAO(URL, USER, PASS);
		
		/* 
		 * ターミナルにて入力受付
		 * 入力された絶対パスの.sqlファイルを読み込み
		 */
		String sql = "";
		Scanner scanner = new Scanner(System.in);
		System.out.println("実行するSQLファイルの絶対パスを入力してください");
		System.out.println(">");
		String filePath = scanner.nextLine();
		scanner.close();
		
		try {
			// sqlの読み込み
			sql = new String(Files.readAllBytes(Paths.get(filePath)),StandardCharsets.UTF_8);
			System.out.println(sql);  // デバッグ用
			
		} catch (Exception e) {
			// TODO: handle exception
		} 
		
		/* SQL分の解析、実行*/
		dao.validationSQL(sql);
//			
//		String filePath = "/Users/ryosuke/Pictures/test.sql";
//        StringBuilder sql = new StringBuilder();
//
//        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
//            String line;
//            while ((line = br.readLine()) != null) {
//                sql.append(line).append("\n");
//            }
//            System.out.println(sql.toString());
//        } catch (IOException e) {
//            e.printStackTrace();
//        }	
		
//			dao.validationSQL(sql.toString());
	}
}