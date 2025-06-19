# quo-test プロジェクト

## プロジェクト概要
### 技術要件
- 言語: Kotlin
- フレームワーク: Spring Boot、jOOQ

### 必要な機能
- 書籍と著者の情報をRDBに登録・更新できる機能
- 著者に紐づく本を取得できる機能
- 書籍の属性
  * タイトル
  * 価格（0以上であること）
  * 著者（最低1人の著者を持つ。複数の著者を持つことが可能）
  * 出版状況（未出版、出版済み。出版済みステータスのものを未出版には変更できない）
- 著者の属性
  * 名前
  * 生年月日（現在の日付よりも過去であること）
  * 著者も複数の書籍を執筆できる

参考：https://quo-digital.hatenablog.com/entry/2024/03/22/143542

### 開発環境
- Project: Gradle - Groovy
- 言語選択: Kotlin
- 追加プラグイン: JOOQ Access Layer, Flyway Migration, PostgreSQL Driver, Docker Compose Support
- Java :21 or 17
- SDKMANやasdf等を利用し、上記で指定したJDKをインストールします。
- IDEはIntelliJ IDEA Community版（業務ではUltimate版を使用しています）を利用します。
- Spring initializr️で生成したプロジェクトをIntelliJ IDEAにインポートし、JDK、Gradleの設定を行い開始します。

### 実装
必要なコントローラーやクラスを追加し、機能を実装していきます。  
データベースの構築にはFlywayを使用し、その後jOOQでコードを自動生成して利用します。

jOOQでコードを自動生成する為に、以下を参照し設定を追加します。
- https://www.jooq.org/doc/3.19/manual/code-generation/codegen-gradle/
- https://www.jooq.org/doc/3.19/manual/code-generation/codegen-configuration/

### 守るべきこと
- 指定された技術要件の適用
- フレームワークやライブラリの適切な利用
- 実行可能性
- 仕様に沿った動作
- 変数名やクラス名、関数名が実態を明確に反映しているか
- Null安全性
- コードフォーマットの整合性
- 変数に再代入しないなど、ベストプラクティスの遵守
- オーバーエンジニアリングしていないか
- 適切な単体テストが作成されているか


## 設計
### テーブル設計
参照：[11.table-design.md](doc/10.design/11.table-design.md)
### API設計
参照：[12.api-design.md](doc/10.design/12.api-design.md)
### パッケージ構成
参照：[15.package-design.md](doc/10.design/15.package-design.md)

## 開発環境構築手順
参照：[11.environment-construction-procedures.md](doc/90.procedure/11.environment-construction-procedures.md)
