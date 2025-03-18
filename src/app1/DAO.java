package app1;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class DAO {
	private String URL;
	private String USER;
	private String PASS;
	private Connection con = null;
	
	/* コンストラクタ
	 * 実行用クラスにて接続処理に必要な以下の情報を設定しておくこと
	    final String URL = "jdbc:mysql://localhost/接続対象のDB名";
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
	
	/*  
	 * SQL 参照 or 更新判定
	 * 【動作】	 : SQL文に対し、参照系、更新系処理かの検査を実施 
	 * 【判定基準】: "SELECT"の文字列で始まるか
	 * 		参照系SQL -> 都度SQL文を引数とし、SELECT用メソッドを呼び出し
	 * 		更新系SQL -> 専用のArrayListに格納し、配列を引数とし更新用メソッドを呼び出し
	 */
	public void validationSQL(String sql) {
		/* 読み込んだSQL文をparseSQLにて整形後、配列に格納 */
		List<String> sqlStatements = parseSQL(sql);
		
		/* 更新処理のみを格納する配列 */
		List<String> updateStatements = new ArrayList<String>();
		/*
		 * 指定されたSQL文の空白を削除し、SELECT文かそれ以外かの判定を行う
		 * 小文字のSQL文にも対応  ## 2025/03/14 更新
		 */
		for (String statement : sqlStatements) {
			if (statement.trim().toUpperCase().startsWith("SELECT")) {
				System.out.println("このSQLは 参照文 です");
				executeQuery(statement);	// SELECT文用のメソッド呼び出し		
			} else {
				System.out.println("このSQLは 更新文 です");
				/*  
				 * 更新系処理はまとめて実施するため、更新処理専用の配列に格納する
				 */
				updateStatements.add(statement);  
			}
		}
		/* 更新処理があればまとめてBatchupdateを実施 */
		if (!updateStatements.isEmpty()) {
			executeBatchUpdate(updateStatements);
		}
		
	}
	
	/* SQL文の整形 */
	public List<String> parseSQL(String sql) {
	 // 受け取ったSQLに対し、整形処理を行い、配列型として返す
	return Arrays.stream(sql.split(";"))  		// SQL文の分割
				 .map(String::trim)				// 空白部分のトリム
				 .filter(s -> !s.isEmpty())		// 空白行をフィルタで除外
				 .collect(Collectors.toList()); // sqlStatementsの配列に格納
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
			 * 【目的】	 ：クエリ結果の見た目を整える
			 * 【ロジック】：① カラム名ごとに文字列長を取得
			 * 　　　　　　　② 一度クエリ結果を走査し、各カラムごとの最長文字列を取得
			 * 　　　　　　　③ ①と②で大きい数字を列幅として採用する            
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
			
			// カラム名とデータの区切り線
			// repeatで列名の全文字の長さ分横に引いておく
			System.out.println("-".repeat(columnNames.toString().length()));
			
			/*
			 * クエリ結果の取得、表示整形
			 */
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
	
	/* 更新(INSERT UPDATE DELETE)用 */
	public void executeBatchUpdate(List<String> updateStatements) {
		connect();
		try (Statement stmt = con.createStatement()){
			
			// Statementオブジェクトにバッチの要素として更新文を追加
			for (String sql : updateStatements) {
				stmt.addBatch(sql);
			}
			/*
			 * 配列のエントリ値がゼロ以上である場合、バッチ要素の処理が成功したことを示します。
			 * その値は、要素の実行により影響を受けたデータベース内の行数を示す更新カウントです。
			 * 値が 2 の場合、要素の処理は成功したが、影響を受けた行数は不明であることを示します。
			 */
			int[] updateCounts = stmt.executeBatch();
			
			for (int i : updateCounts) {
				System.out.println(i);
			}
			
			// 配列の要素、つまりexecuteBatchの戻り値を合計して、更新された行数を取得して出力
			int totalUpdate = Arrays.stream(updateCounts).sum();
			System.out.println(totalUpdate + "件データを更新しました。");
			
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("データの更新処理に失敗しました。");
		}
		disconnect();
		
	}
}