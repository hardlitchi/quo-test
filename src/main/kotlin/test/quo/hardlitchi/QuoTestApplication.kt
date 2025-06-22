package test.quo.hardlitchi

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

/**
 * Spring Boot メインアプリケーションクラス
 * 
 * 書籍と著者管理システムのエントリーポイント
 * データベースアクセスにjOOQ、マイグレーションにFlywayを使用
 */
@SpringBootApplication
class QuoTestApplication

/**
 * アプリケーション起動メソッド
 * 
 * @param args コマンドライン引数
 */
fun main(args: Array<String>) {
    runApplication<QuoTestApplication>(*args)
}