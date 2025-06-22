package test.quo.hardlitchi.common.config

import com.fasterxml.jackson.annotation.JsonInclude
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder

/**
 * Jackson ObjectMapper のカスタマイズ設定
 */
@Configuration
class JacksonConfig {

    /**
     * Jackson ObjectMapper のグローバル設定カスタマイザーを提供します。
     * - シリアライズ時に null の値を持つフィールドを除外します。
     *
     * @return Jackson2ObjectMapperBuilderCustomizer インスタンス
     */
    @Bean
    fun jackson2ObjectMapperBuilderCustomizer(): Jackson2ObjectMapperBuilderCustomizer {
        return Jackson2ObjectMapperBuilderCustomizer { jacksonObjectMapperBuilder: Jackson2ObjectMapperBuilder ->
            // JSONシリアライズ時にnull値のフィールドを含めないように設定
            jacksonObjectMapperBuilder.serializationInclusion(JsonInclude.Include.NON_NULL)
        }
    }
}