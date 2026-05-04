package ch.ejpd.example.precrime.infrastructure.integration.persistence

import ch.ejpd.example.precrime.domain.AggregateVersion
import org.jooq.Converter
import org.jooq.Field
import org.jooq.impl.DSL
import org.jooq.impl.SQLDataType
import java.util.*

fun versionField(name: String): Field<AggregateVersion> {
    val converter = object : Converter<Long, AggregateVersion> {
        override fun from(databaseObject: Long?): AggregateVersion? = databaseObject?.let { AggregateVersion(it) }
        override fun to(userObject: AggregateVersion?): Long? = userObject?.value
        override fun fromType(): Class<Long> = Long::class.java
        override fun toType(): Class<AggregateVersion> = AggregateVersion::class.java
    }
    return DSL.field(DSL.name(name), SQLDataType.BIGINT.asConvertedDataType(converter))
}

fun <ID : Any> uuidField(name: String, type: Class<ID>, from: (UUID) -> ID, to: (ID) -> UUID): Field<ID> {
    val converter = object : Converter<UUID, ID> {
        override fun from(databaseObject: UUID?): ID? = databaseObject?.let(from)
        override fun to(userObject: ID?): UUID? = userObject?.let(to)
        override fun fromType(): Class<UUID> = UUID::class.java
        override fun toType(): Class<ID> = type
    }
    return DSL.field(DSL.name(name), SQLDataType.UUID.asConvertedDataType(converter))
}

fun <E : Enum<E>> enumField(name: String, type: Class<E>): Field<E> {
    val converter = object : Converter<String, E> {
        override fun from(databaseObject: String?): E? = databaseObject?.let { java.lang.Enum.valueOf(type, it) }
        override fun to(userObject: E?): String? = userObject?.name
        override fun fromType(): Class<String> = String::class.java
        override fun toType(): Class<E> = type
    }
    return DSL.field(DSL.name(name), SQLDataType.VARCHAR.asConvertedDataType(converter))
}
