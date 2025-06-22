-- 書籍テーブル作成
CREATE TABLE books (
    title VARCHAR(500) PRIMARY KEY,  -- 書籍タイトル（主キー） 
    price DECIMAL(10,2) NOT NULL,    -- 価格（0以上であること）
    publication_status VARCHAR(20) NOT NULL DEFAULT 'UNPUBLISHED',  -- 出版状況
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,  -- 作成日時
    created_by VARCHAR(255) NOT NULL DEFAULT 'system',        -- 作成者
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,  -- 更新日時
    updated_by VARCHAR(255) NOT NULL DEFAULT 'system',        -- 更新者
    
    -- 価格は0以上であることを制約で保証
    CONSTRAINT chk_price_non_negative CHECK (price >= 0),
    
    -- 出版状況は'UNPUBLISHED'または'PUBLISHED'のみ
    CONSTRAINT chk_publication_status CHECK (publication_status IN ('UNPUBLISHED', 'PUBLISHED'))
);

-- 書籍テーブルの更新日時自動更新トリガー
CREATE TRIGGER update_books_updated_at 
    BEFORE UPDATE ON books 
    FOR EACH ROW 
    EXECUTE FUNCTION update_updated_at_column();

-- 出版済みの書籍を未出版に戻すことを防ぐトリガー関数
CREATE OR REPLACE FUNCTION prevent_unpublish_published_book()
RETURNS TRIGGER AS $$
BEGIN
    -- 既存の状態が'PUBLISHED'で、新しい状態が'UNPUBLISHED'の場合はエラー
    IF OLD.publication_status = 'PUBLISHED' AND NEW.publication_status = 'UNPUBLISHED' THEN
        RAISE EXCEPTION '出版済みの書籍を未出版状態に戻すことはできません。書籍タイトル: %', OLD.title;
    END IF;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- 出版済み書籍の未出版への変更を防ぐトリガー
CREATE TRIGGER prevent_book_unpublish 
    BEFORE UPDATE ON books 
    FOR EACH ROW 
    EXECUTE FUNCTION prevent_unpublish_published_book();