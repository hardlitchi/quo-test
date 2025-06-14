# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview
This is a Kotlin Spring Boot application for managing books and authors using jOOQ for database access and Flyway for migrations. The system handles book and author information with proper validation and business rules.

## Technology Stack
- Language: Kotlin
- Framework: Spring Boot
- Database Access: jOOQ
- Migration: Flyway
- Database: PostgreSQL
- Build Tool: Gradle
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
Since this project uses Gradle, common commands include:
- `./gradlew build` - Build the project
- `./gradlew test` - Run tests
- `./gradlew bootRun` - Run the application
- `./gradlew generateJooq` - Generate jOOQ classes (after Flyway migrations)

## Database Setup
1. Use Flyway for database migrations
2. Run migrations to create schema
3. Generate jOOQ classes using Gradle plugin
4. Configure jOOQ code generation following official documentation

## Code Standards
- Use Kotlin best practices (immutability, null safety)
- Follow Spring Boot conventions
- Maintain proper code formatting consistency
- Create comprehensive unit tests
- Use meaningful variable, class, and function names
- Avoid over-engineering solutions
- Apply validation rules as specified in business requirements