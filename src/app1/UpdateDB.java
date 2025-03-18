package app1;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;

public class UpdateDB {
	public static void main(String args[]) {
		
		/* DB接続情報 */
	    final String URL = "jdbc:mysql://localhost/shop";
		final String USER = "root";
		final String PASS = "";
		
		/* Data Access Objectの生成 */
		DAO dao = new DAO(URL, USER, PASS);
		
		/* 
		 * ターミナルにて入力受付
		 * 入力された絶対パスの.sql内のSQL文を読み込む
		 */
		String sql = "";
		Scanner scanner = new Scanner(System.in);
		System.out.println("実行する.sqlファイルの絶対パスを入力してください。");
		System.out.println(">");
		// 入力内容からファイルの絶対パスを取得
		String filePath = scanner.nextLine();
		scanner.close();
		
		try {
			// sqlの読み込み
			sql = new String(Files.readAllBytes(Paths.get(filePath)),StandardCharsets.UTF_8);
			System.out.println(sql);  // デバッグ用
			
		} catch (Exception e) {
			e.printStackTrace();
		} 
		
		/* SQLの解析、実行*/
		dao.validationSQL(sql);
	}
}