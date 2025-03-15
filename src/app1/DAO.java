package app1;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class DAO {
	private String URL;
	private String USER;
	private String PASS;
	private Connection con = null;
	
	/* コンストラクタ
	 * 実行用クラスにて接続処理に必要な以下の情報を設定しておくこと
	    final String URL = "jdbc::mysql://localhost/接続対象のDB名";
		final String USER = "ユーザ名";
		final String PASS = "パスワード";
	 */
	public DAO(String URL, String USER, String PASS) {
		this.URL = URL;
		this.USER = USER;
		this.PASS = PASS;
	}

	/* 接続処理 */
	public void connect() {
		System.out.println("接続処理開始");
		System.out.println(URL + " に　" + USER + "で接続します");
		try {
			con = DriverManager.getConnection(URL, USER, PASS);
			System.out.println("DB接続 成功 しました");
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("DB接続 失敗 しました");
		}
	}	
	
	/* 切断処理 */
	public void disconnect() {
		try {
			if (con != null) con.close();
			System.out.println("DBとの接続を終了しました。");
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("DBとの接続を終了中にエラーが発生しました。");
		}
		
	}
	
	/* SQL 参照 or 更新判定 */
	public void validationSQL(String sql) {
		/*
		 * 指定されたSQL文の空白を削除し、SELECT文か、それ以外かの判定を行う
		 * 小文字のSQL文にも対応  ## 2025/03/14 更新
		 */
		if (sql.trim().toUpperCase().startsWith("SELECT")) {
			System.out.println("このSQLは 参照文 です");
			executeQuery(sql);	// SELECT文用のメソッド呼び出し		
		} else {
			System.out.println("このSQLは 更新文 です");
			executeUpdate(sql); // 更新文用メソッド呼び出し
		}
		
	}
	
	/* SELECT用 */
	public void executeQuery (String sql) {
		// DB接続処理
		connect();
		/* 
		 * try-with-resources形式で記述
		 * Statement, ResultSetはいずれもAutoCloseablインターフェースの実装クラス
		 * ResultSetのポインターを一度リセットしたいため、オプションを設定
		 */
		 
		try (Statement stmt = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			 ResultSet rs = stmt.executeQuery(sql)){
			
			ResultSetMetaData meta = rs.getMetaData();  	// クエリ結果を取得するオブジェクト
			int columnCount = meta.getColumnCount();    	// クエリ結果の "カラム数" を取得するオブジェクト
			int[] columnWidths = new int[columnCount];  	// クエリ結果の "各カラムの長さ" を格納する配列
			List<String> columnNames = new ArrayList<>(); 	// クエリ結果の "カラム名" を格納する配列 
			
			/*
			 * カラム名の最大幅を決定する
			 * 長さの下限値を10と仮定
			 * getColumnNamesでカラム名の長さを取得し、より大きい値を返す
			 * この処理を取得したすべてのカラム名に対して実施する
			 */
			for(int i = 1; i <= columnCount; i++) {
				columnWidths[i - 1] = Math.max(meta.getColumnName(i).length(), 10);
			}
			
			while (rs.next()) {
				for(int i = 1; i <= columnCount; i++) {
					int dataLength = rs.getString(i) != null ? rs.getString(i).length() : 4;
					columnWidths[i - 1] = Math.max(columnWidths[i - 1], dataLength);
				}
			}
			
			/* カーソルを先頭に戻す */ 
			rs.beforeFirst();
			
			/* カラム名を配列に格納 */
			for (int i = 1; i <= columnCount ; i ++) {
				/* formatメソッドで、最大幅に統一した文字列を左詰めで配列に格納 */
				columnNames.add(String.format("%-" + columnWidths[i - 1] + "s", meta.getColumnName(i)));
			}
			System.out.println(String.join(" | ", columnNames));
			
			// 列名とデータの区切り線
			System.out.println("-".repeat(columnNames.toString().length()));
			
			while (rs.next()) {
				List<String> rowData = new ArrayList<>();
				
				for (int i = 1; i <= columnCount ; i ++) {
					rowData.add(String.format("%-" + columnWidths[i - 1] + "s", rs.getString(i)));
				}
				
				System.out.println(String.join(" | ", rowData));
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		disconnect();
	}
	
	/* INSERT UPDATE DELETE用 */
	public void executeUpdate(String sql) {
		int result = 0;
		connect();
		try (Statement stmt = con.createStatement()){
			
			result = stmt.executeUpdate(sql); 
			System.out.println(result + "件データを更新しました。");
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("データの更新処理に失敗しました。");
		}
		disconnect();
	}
}
