package com.h2Invent.skibin

import org.json.JSONObject

data class ChildListItem(
    val name: String,
    val school: String,
    val grade: Int,
    val checkedIn: Boolean,
    val krank: Boolean,
    val krankVon: String?,
    val krankBis: String?,
    val krankBemerkung: String?,
    val detailUrl: String,
    val schoolId: Int,
    val hasBirthday: Boolean,
    val checkinUrl: String,
)

data class Schule(
    val id: Int,
    val name: String,
) {
    override fun toString(): String = name
}

data class UserConnectionInfo(
    val name: String,
    val organisation: String,
    val email: String,
)

fun JSONObject.toChildListItem(): ChildListItem = ChildListItem(
    name = "${optString("vorname")} ${optString("name")}".trim(),
    school = optString("schule"),
    grade = optInt("klasse"),
    checkedIn = optBoolean("checkin"),
    krank = optBoolean("krank"),
    krankVon = optNullableString("krankVon"),
    krankBis = optNullableString("krankBis"),
    krankBemerkung = optNullableString("krankBemerkung"),
    detailUrl = optString("detail"),
    schoolId = optInt("schuleId"),
    hasBirthday = optBoolean("hasBirthday"),
    checkinUrl = optString("checkinUrl"),
)

private fun JSONObject.optNullableString(key: String): String? =
    optString(key).takeIf { it.isNotBlank() && it != "null" }
