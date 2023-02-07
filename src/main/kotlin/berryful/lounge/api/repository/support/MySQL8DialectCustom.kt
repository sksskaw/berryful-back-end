package berryful.lounge.api.repository.support

import org.hibernate.dialect.MySQL8Dialect
import org.hibernate.dialect.function.SQLFunctionTemplate
import org.hibernate.type.StandardBasicTypes

class MySQL8DialectCustom : MySQL8Dialect() {
    init {
        registerFunction("postFullTextSearch", SQLFunctionTemplate(StandardBasicTypes.BOOLEAN, "match(?1,?2) against (?3 in boolean mode) and 1"))
    }
}