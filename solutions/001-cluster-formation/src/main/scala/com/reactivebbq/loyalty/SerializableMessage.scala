package com.reactivebbq.loyalty

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "type")
trait SerializableMessage
