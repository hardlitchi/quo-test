-- 著者テーブル作成
CREATE TABLE authors (
    name VARCHAR(255) PRIMARY KEY,  -- 著者名（主キー）
    birth_date DATE NOT NULL,       -- 生年月日（現在の日付より過去であること）
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,  -- 作成日時
    created_by VARCHAR(255) NOT NULL DEFAULT 'system',        -- 作成者
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,  -- 更新日時
    updated_by VARCHAR(255) NOT NULL DEFAULT 'system',        -- 更新者
    
    -- 生年月日は現在の日付より過去であることを制約で保証
    CONSTRAINT chk_birth_date_past CHECK (birth_date < CURRENT_DATE)
);

-- 更新日時の自動更新のためのトリガー関数
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- 著者テーブルの更新日時自動更新トリガー
CREATE TRIGGER update_authors_updated_at 
    BEFORE UPDATE ON authors 
    FOR EACH ROW 
    EXECUTE FUNCTION update_updated_at_column();