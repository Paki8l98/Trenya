package space.khay.trenya

import android.os.Parcel
import android.os.Parcelable
import com.google.firebase.database.DataSnapshot

data class Exercise(
    val id: String,
    val name: String,
    val muscleGroup: String,
    val duration: Int,
    val imageUrl: String,
    val gifPath: String,
    val recommendations: String,
    val sets: Int = (3..5).random(),
    val repetitions: Int = 5,
    val weight: Double = 0.0,
    var isSelected: Boolean = false
) : Parcelable {
    val durationSeconds: Int
        get() = duration * 1

    constructor(snapshot: DataSnapshot) : this(
        id = snapshot.key ?: "",
        name = snapshot.child("name").value.toString(),
        muscleGroup = snapshot.child("muscleGroup").value.toString(),
        duration = snapshot.child("duration").value.toString().toInt(),
        imageUrl = snapshot.child("imageUrl").value.toString(),
        gifPath = snapshot.child("gifPath").value.toString(),
        recommendations = snapshot.child("recommendations").value.toString(),
        weight = snapshot.child("weight").value.toString().toDouble()
    )

    constructor() : this(
        "",
        "",
        "",
        0,
        "",
        "",
        ""
    )

    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readInt(),
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readInt(),
        parcel.readInt(),
        parcel.readDouble(),
        parcel.readByte() != 0.toByte()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(name)
        parcel.writeString(muscleGroup)
        parcel.writeInt(duration)
        parcel.writeString(imageUrl)
        parcel.writeString(gifPath)
        parcel.writeString(recommendations)
        parcel.writeInt(sets)
        parcel.writeInt(repetitions)
        parcel.writeDouble(weight)
        parcel.writeByte(if (isSelected) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Exercise> {
        override fun createFromParcel(parcel: Parcel): Exercise {
            return Exercise(parcel)
        }

        override fun newArray(size: Int): Array<Exercise?> {
            return arrayOfNulls(size)
        }
    }
}
