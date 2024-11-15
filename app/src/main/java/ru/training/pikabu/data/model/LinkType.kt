package ru.training.pikabu.data.model

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(with = LinkType.LinkTypeSerializer::class)
sealed class LinkType {
    @Serializable
    data object Internal : LinkType()

    @Serializable
    data object External : LinkType()

    object LinkTypeSerializer : KSerializer<LinkType> {
        override val descriptor: SerialDescriptor =
            PrimitiveSerialDescriptor("LinkType", PrimitiveKind.STRING)

        override fun serialize(encoder: Encoder, value: LinkType) =
            encoder.encodeString(
                when (value) {
                    is Internal -> "Internal"
                    is External -> "External"
                }
            )

        override fun deserialize(decoder: Decoder) =
            when (val linkTypeString = decoder.decodeString()) {
                "Internal" -> Internal
                "External" -> External
                else -> throw SerializationException("Неизвестный тип ссылки: $linkTypeString")
            }
    }
}
