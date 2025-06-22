-- 出版テーブル作成（書籍と著者の多対多関係を管理）
CREATE TABLE publications (
    book_title VARCHAR(500) NOT NULL,     -- 書籍タイトル
    author_name VARCHAR(255) NOT NULL,    -- 著者名
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,  -- 作成日時
    created_by VARCHAR(255) NOT NULL DEFAULT 'system',        -- 作成者
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,  -- 更新日時
    updated_by VARCHAR(255) NOT NULL DEFAULT 'system',        -- 更新者
    
    -- 複合主キー（書籍タイトル + 著者名）
    PRIMARY KEY (book_title, author_name),
    
    -- 外部キー制約
    CONSTRAINT fk_publications_book 
        FOREIGN KEY (book_title) 
        REFERENCES books(title) 
        ON DELETE CASCADE 
        ON UPDATE CASCADE,
        
    CONSTRAINT fk_publications_author 
        FOREIGN KEY (author_name) 
        REFERENCES authors(name) 
        ON DELETE CASCADE 
        ON UPDATE CASCADE
);

-- 出版テーブルの更新日時自動更新トリガー
CREATE TRIGGER update_publications_updated_at 
    BEFORE UPDATE ON publications 
    FOR EACH ROW 
    EXECUTE FUNCTION update_updated_at_column();

-- 書籍には最低1人の著者が必要であることを保証するトリガー関数
CREATE OR REPLACE FUNCTION ensure_book_has_author()
RETURNS TRIGGER AS $$
BEGIN
    -- 削除操作の場合、該当書籍に他の著者が存在するかチェック
    IF TG_OP = 'DELETE' THEN
        IF NOT EXISTS (
            SELECT 1 FROM publications 
            WHERE book_title = OLD.book_title 
            AND author_name != OLD.author_name
        ) THEN
            RAISE EXCEPTION '書籍には最低1人の著者が必要です。書籍タイトル: %', OLD.book_title;
        END IF;
        RETURN OLD;
    END IF;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- 書籍の最後の著者削除を防ぐトリガー
CREATE TRIGGER ensure_book_author_exists 
    BEFORE DELETE ON publications 
    FOR EACH ROW 
    EXECUTE FUNCTION ensure_book_has_author();