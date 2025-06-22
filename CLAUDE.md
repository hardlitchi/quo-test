# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 言語設定
- プロジェクト内でのやり取りやコメントは日本語で行う
- コードのコメントも日本語で記述する

## Project Overview
This is a Kotlin Spring Boot application for managing books and authors using jOOQ for database access and Flyway for migrations. The system handles book and author information with proper validation and business rules.

## Technology Stack
- Language: Kotlin
- Framework: Spring Boot 3.3.4
- Database Access: jOOQ 3.19.13
- Migration: Flyway
- Database: PostgreSQL 15
- Build Tool: Gradle 8.5
- Testing: JUnit 5, TestContainers, Mockito-Kotlin
- Java Version: 21 or 17

## Package Structure
- `test.quo.hardlitchi` - Root package
- `test.quo.hardlitchi.common.bean` - DTOs/Beans
- `test.quo.hardlitchi.common.const` - Constants
- `test.quo.hardlitchi.common.entity` - Database entities
- `test.quo.hardlitchi.common.repository` - Data access layer
- `test.quo.hardlitchi.common.service` - Business logic
- `test.quo.hardlitchi.common.util` - Utilities  
- `test.quo.hardlitchi.web.bean` - Web request/response objects
- `test.quo.hardlitchi.web.controller` - REST controllers

## Core Domain Model
### Books
- Primary Key: Title
- Attributes: title, price (≥0), publication status, timestamps, audit fields
- Business Rule: Published books cannot be changed back to unpublished status
- Relationships: Must have at least one author, can have multiple authors

### Authors  
- Primary Key: Name
- Attributes: name, birth date (must be in past), timestamps, audit fields
- Relationships: Can write multiple books

### Publications (Junction Table)
- Primary Key: Book title + Author name
- Links books to their authors

## API Endpoints
- Author registration and updates
- Book and publication registration and updates  
- Retrieve all books by a specific author

## Development Commands
### Essential Commands
- `./gradlew build` - プロジェクトをビルド
- `./gradlew test` - 全テストを実行
- `./gradlew bootRun` - アプリケーションを起動（ポート8081）
- `./gradlew flywayMigrate` - Flywayマイグレーションを実行
- `./gradlew generateJooq` - jOOQクラスを生成（マイグレーション後）

### 権限エラー対策
- ビルドディレクトリの権限エラーが発生する場合は `sudo` を使用
- `sudo ./gradlew clean test jacocoTestReport` - 管理者権限でテスト実行とカバレッジ生成
- `sudo rm -rf build` - ビルドディレクトリの強制削除

### Database Operations
- `docker compose up -d` - PostgreSQLコンテナを起動
- `./gradlew flywayInfo` - マイグレーション状態を確認
- `./gradlew flywayClean` - データベースをクリーン（開発環境のみ）

### Testing
- `./gradlew test --tests "ClassName"` - 特定のテストクラスを実行
- `./gradlew test --tests "ClassName.methodName"` - 特定のテストメソッドを実行
- `./gradlew test jacocoTestReport` - テストカバレッジレポート生成

## Database Setup
### Development Environment
1. `docker compose up -d` でPostgreSQLコンテナを起動
2. Database: `quo_test`, User: `quo_user`, Password: `quo_pass`, Port: 5432
3. `./gradlew flywayMigrate` でマイグレーションを実行
4. `./gradlew generateJooq` でjOOQクラスを生成
5. 生成されたクラスは `src/main/generated` に配置される

### jOOQ Configuration
- パッケージ: `test.quo.hardlitchi.generated`
- Records生成、Immutable POJOs生成、Fluent Setters有効

## Architecture Notes
### Data Flow
1. **Controllers** (`web.controller`) - REST APIエンドポイント、リクエスト/レスポンス処理
2. **Services** (`common.service`) - ビジネスロジック、バリデーション
3. **Repositories** (`common.repository`) - jOOQを使用したデータアクセス
4. **Entities** (`common.entity`) - データベーステーブルに対応

### Key Business Rules
- 書籍は最低1人の著者が必要（多対多関係）
- 書籍価格は0以上
- 出版済み書籍は未出版に戻せない
- 著者の生年月日は現在日付より過去
- 主キー: 書籍（タイトル）、著者（名前）、出版（書籍タイトル+著者名）

### Spring Boot Configuration
- アプリケーションポート: 8081
- Flyway有効化（automatic migration実行）
- DataSourceAutoConfiguration、HibernateJpaAutoConfigurationは無効化（jOOQ使用のため）
- TestContainersを使用したテスト環境

## Current Implementation Status (2025年6月22日時点)

### 🎯 100% Test Coverage Achievement!
- **Database Design**: 著者・書籍・出版の3テーブル完全実装
- **Full Stack Implementation**: Entity→Repository→Service→Controller→Web Bean
- **REST API**: 著者・書籍管理の完全なCRUD API
- **Exception Handling**: グローバル例外ハンドラによる統一的エラー処理
- **Test Suite**: **130+テスト、100%カバレッジ達成** ⭐️

### Test Coverage (Latest)
- **Overall Coverage**: **100%** (instruction coverage) ⭐️
- **Branch Coverage**: **100%** (分岐カバレッジ) ⭐️
- **Total Tests**: **130+ tests** (包括的テストスイート)
- **Package Coverage** (全パッケージ100%達成):
  - web.controller: 100% ✅
  - web.bean: 100% ✅  
  - web.exception: 100% ✅
  - common.repository: 100% ✅
  - common.service: 100% ✅
  - common.entity: 100% ✅

### Technical Achievements
- **Mockito-Kotlin**: any()メソッドのオーバーロード問題解決済み
- **TestRestTemplate**: 統合テストでの実HTTP通信
- **UUID Test Data**: テストデータ競合回避による安定したテスト実行
- **@DirtiesContext**: テスト間の独立性確保
- **jOOQ Type Safety**: コンパイル時のSQLタイプチェック
- **Boundary Value Testing**: 境界値テストの完全実装
- **Exception Path Coverage**: 全例外処理パターンのカバー
- **DTO Testing**: 全DTOクラスの完全テスト

### Build & Quality Status
- ✅ All dependencies resolved successfully
- ✅ All 130+ tests passing
- ✅ Complete CRUD operations functional
- ✅ Error handling with proper HTTP status codes
- ✅ Database constraints and business rules enforced
- ✅ **Production-quality test coverage achieved**