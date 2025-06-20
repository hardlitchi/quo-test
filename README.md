# quo-test プロジェクト

## プロジェクト概要

Kotlin + Spring Boot + jOOQを使用した書籍・著者管理システムです。
PostgreSQLをデータベースとし、Flywayによるマイグレーション管理、TestContainersによる実環境テストを特徴とします。

### 技術スタック
- **言語**: Kotlin
- **フレームワーク**: Spring Boot 3.3.4
- **データベースアクセス**: jOOQ 3.19.13
- **データベース**: PostgreSQL 15
- **マイグレーション**: Flyway
- **ビルドツール**: Gradle 8.5
- **テスト**: JUnit 5, TestContainers, Mockito-Kotlin
- **Java バージョン**: 21 or 17

### 実装済み機能

#### 📚 書籍管理機能
- **書籍登録・更新・削除**: タイトル、価格、出版状況の管理
- **出版状況制御**: 出版済み→未出版への変更制限
- **価格制約**: 0以上の価格設定
- **著者関連付け**: 最低1人、複数著者対応

#### 👤 著者管理機能  
- **著者登録・更新・削除**: 名前、生年月日の管理
- **生年月日制約**: 過去日付のみ受付
- **書籍関連付け**: 複数書籍の執筆対応

#### 🔗 出版関係管理
- **多対多関係**: 書籍↔著者の中間テーブル管理
- **参照整合性**: 外部キー制約による整合性保証

#### 🌐 REST API
- **著者API**: 登録、更新、取得、一覧、削除
- **書籍API**: 登録、更新、取得、一覧、削除  
- **統一レスポンス**: 成功・エラー時の一貫したJSON形式
- **例外ハンドリング**: グローバル例外ハンドラによる適切なエラー処理

### アーキテクチャ

```
src/main/kotlin/test/quo/hardlitchi/
├── QuoTestApplication.kt                    # Spring Boot メインクラス
├── common/
│   ├── entity/                             # ドメインモデル (Author, Book, Publication)
│   ├── repository/                         # データアクセス層 (jOOQ)
│   └── service/                           # ビジネスロジック層
└── web/
    ├── bean/                              # Web リクエスト・レスポンス
    ├── controller/                        # REST API エンドポイント
    └── exception/                         # グローバル例外ハンドラ
```

### 品質指標

#### テストカバレッジ（2025年6月20日時点）
- **全体カバレッジ**: 93% (命令カバレッジ)
- **ブランチカバレッジ**: 70%  
- **テスト数**: 125テスト

#### パッケージ別カバレッジ
- **web.controller**: 100% ✅
- **web.bean**: 100% ✅
- **web.exception**: 98% ✅
- **common.repository**: 95% ✅
- **common.service**: 93% ✅
- **common.entity**: 73% 🔄

#### ビルド・テスト状況
- **ビルド**: ✅ 全依存関係が正常に解決
- **テスト**: ✅ 全125テストが正常実行
- **API動作**: ✅ 完全なCRUD操作が可能

## 使用方法

### 🚀 クイックスタート

```bash
# 1. PostgreSQLコンテナを起動
docker compose up -d

# 2. データベースマイグレーション実行
./gradlew flywayMigrate

# 3. jOOQクラス生成
./gradlew generateJooq

# 4. アプリケーション起動
./gradlew bootRun
```

### 🧪 テスト実行

```bash
# 全テスト実行
./gradlew test

# テストレポート生成
./gradlew test jacocoTestReport

# 特定のテストクラス実行
./gradlew test --tests "AuthorRepositoryTest"
```

### 📊 開発コマンド

```bash
# ビルド
./gradlew build

# マイグレーション状態確認
./gradlew flywayInfo

# データベースクリーン（開発環境のみ）
./gradlew flywayClean

# jOOQクラス再生成
./gradlew generateJooq
```

## API仕様

### 著者管理API

| メソッド | エンドポイント | 説明 |
|---------|---------------|------|
| POST | `/api/authors` | 著者登録 |
| GET | `/api/authors` | 著者一覧取得 |
| GET | `/api/authors/{name}` | 著者取得 |
| PUT | `/api/authors/{name}` | 著者更新 |
| DELETE | `/api/authors/{name}` | 著者削除 |

### 書籍管理API

| メソッド | エンドポイント | 説明 |
|---------|---------------|------|
| POST | `/api/books` | 書籍登録 |
| GET | `/api/books` | 書籍一覧取得 |
| GET | `/api/books/{title}` | 書籍取得 |
| PUT | `/api/books/{title}` | 書籍更新 |
| DELETE | `/api/books/{title}` | 書籍削除 |
| GET | `/api/books/author/{authorName}` | 著者別書籍一覧 |

### レスポンス形式

```json
{
  "success": true,
  "data": { ... },
  "message": "操作が成功しました",
  "errors": null
}
```

## 設計ドキュメント

### 詳細設計
- **テーブル設計**: [11.table-design.md](doc/10.design/11.table-design.md)
- **API設計**: [12.api-design.md](doc/10.design/12.api-design.md)
- **例外設計**: [13.exception-design.md](doc/10.design/13.exception-design.md)
- **DTO設計**: [14.dto-bean-design.md](doc/10.design/14.dto-bean-design.md)
- **パッケージ構成**: [15.package-design.md](doc/10.design/15.package-design.md)
- **テスト設計**: [18.test-design.md](doc/10.design/18.test-design.md)

### 手順書
- **開発ワークフロー**: [10.development-workflow.md](doc/90.procedure/10.development-workflow.md)
- **環境構築手順**: [11.environment-construction-procedures.md](doc/90.procedure/11.environment-construction-procedures.md)

## 開発環境構築

### 前提条件
- **Java**: 21 or 17 (SDKMAN推奨)
- **Docker**: PostgreSQLコンテナ実行用
- **IDE**: IntelliJ IDEA (Community/Ultimate)

### セットアップ手順
詳細は [環境構築手順書](doc/90.procedure/11.environment-construction-procedures.md) を参照してください。

## ビジネスルール

### 書籍制約
- **価格**: 0以上であること
- **著者**: 最低1人の著者が必要
- **出版状況**: 出版済み→未出版への変更不可

### 著者制約  
- **生年月日**: 現在日付より過去であること
- **名前**: 必須項目、空文字不可

### データ整合性
- **参照整合性**: 外部キー制約による保証
- **トランザクション**: Spring Bootの@Transactionalによる制御
- **楽観的ロック**: created_at/updated_atによるタイムスタンプ管理

## トラブルシューティング

### よくある問題

#### jOOQクラスが見つからない
```bash
./gradlew generateJooq
```

#### データベース接続エラー
```bash
docker compose up -d
docker compose ps
```

#### テスト失敗
```bash
./gradlew test --tests "ClassName" --info
# build/reports/tests/test/index.html でレポート確認
```

## ライセンス

このプロジェクトは学習・評価目的で作成されています。

## 参考資料

- [元記事](https://quo-digital.hatenablog.com/entry/2024/03/22/143542)
- [jOOQ Documentation](https://www.jooq.org/doc/3.19/manual/)
- [Spring Boot Reference](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [TestContainers Documentation](https://www.testcontainers.org/)