package com.eilas.newcourseschedule.data.model

import android.os.Parcel
import android.os.Parcelable

data class LoggedInUser(
    val id: String,
    val pwd: String,
    val name: String = id,
    val sex: Sex = Sex.MALE
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readString().toString(),
        if (parcel.readString().toString().equals(Sex.MALE.toString()))
            Sex.MALE
        else
            Sex.FEMALE
    )

    enum class Sex(i: Int) {
        MALE(0), FEMALE(1)
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(pwd)
        parcel.writeString(name)
        parcel.writeString(sex.toString())
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<LoggedInUser> {
        override fun createFromParcel(parcel: Parcel): LoggedInUser {
            return LoggedInUser(parcel)
        }

        override fun newArray(size: Int): Array<LoggedInUser?> {
            return arrayOfNulls(size)
        }
    }
}