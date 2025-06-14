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
### パッケージ構成
- test.quo.hardlitchi - ルートパッケージ
- test.quo.hardlitchi.common - 共通部品
- test.quo.hardlitchi.common.bean - bean
- test.quo.hardlitchi.common.const - 定数
- test.quo.hardlitchi.common.entity - エンティティ
- test.quo.hardlitchi.common.repository - リポジトリ
- test.quo.hardlitchi.common.service - 業務ロジック
- test.quo.hardlitchi.common.util - ユーティリティ
- test.quo.hardlitchi.web.bean - Webパラメータ
- test.quo.hardlitchi.web.controller - コントローラ

### テーブル設計
#### 書籍
主キー：タイトル
- 名前
- 価格（0以上であること）
- 出版状況（未出版、出版済み。出版済みステータスのものを未出版には変更できない）
- 作成日時
- 作成者
- 更新日時
- 更新者

#### 著者
主キー：名前
- 名前
- 生年月日（現在の日付よりも過去であること）
- 作成日時
- 作成者
- 更新日時
- 更新者

#### 出版
主キー：書籍タイトル、著者名
- 書籍タイトル
- 著者名
- 作成日時
- 作成者
- 更新日時
- 更新者

### API設計
- 著者情報登録
- 著者情報更新
- 著者出版書籍一覧取得  
  当該著者が出版した書籍一覧を取得する
- 書籍、出版情報登録
- 書籍、出版情報更新
