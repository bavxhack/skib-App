package com.h2Invent.skibin

import org.json.JSONObject

data class ChildListItem(
    val name: String,
    val school: String,
    val grade: Int,
    val checkedIn: Boolean,
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
    detailUrl = optString("detail"),
    schoolId = optInt("schuleId"),
    hasBirthday = optBoolean("hasBirthday"),
    checkinUrl = optString("checkinUrl"),
)
