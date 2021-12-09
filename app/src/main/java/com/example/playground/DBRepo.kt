package com.example.playground

import android.util.Log
import androidx.room.*
import androidx.room.OnConflictStrategy.REPLACE
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*

object DBRepo {

    private val db: AppDatabase = Room.databaseBuilder(
        MyApp.appContext!!,
        AppDatabase::class.java, "database-name"
    ).fallbackToDestructiveMigration().build()

    fun getAllUsers(): Flow<List<User>> {
        return db.userDao().getAll()
    }

    @OptIn(FlowPreview::class)
    fun getPageUsers(offsetFlow: MutableSharedFlow<Int>): Flow<List<User>> {
        return offsetFlow.distinctUntilChanged().flatMapLatest { offset ->
            Log.d("DBRepo", "got offset: $offset")
            val pageSize = PagedDataProvider.DB_LIMIT
            Log.d("DBRepo", "requesting new flow DB with $offset and pageSize: $pageSize")
            return@flatMapLatest getUsers(offset, pageSize)
        }
    }

    suspend fun getUsers(
        offset: Int,
        pageSize: Int
    ) = db.userDao().getAll(offset, pageSize)

    suspend fun createData() {
        val usersList = mutableListOf<User>()
        for (id in 0 until 1000 step 2) {
            usersList.add(User(id, "$id + name", "$id + lastname"))
        }
        db.userDao().insertAll(*usersList.toTypedArray())
    }

    suspend fun clearData() {
        db.userDao().deleteAll()
    }


    suspend fun addUsers(usersList: List<User>) {
        db.userDao().insertAll(*usersList.toTypedArray())
    }

    suspend fun getTotalUsers(): Int {
        return db.userDao().getSize()
    }

}

@Database(entities = [User::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
}

@Entity
data class User(
    @PrimaryKey val uid: Int,
    @ColumnInfo(name = "first_name") val firstName: String?,
    @ColumnInfo(name = "last_name") val lastName: String?
)

@Dao
interface UserDao {
    @Query("SELECT * FROM user")
    fun getAll(): Flow<List<User>>

    @Query("SELECT * FROM user WHERE uid IN (:userIds)")
    fun loadAllByIds(userIds: IntArray): List<User>

    @Query(
        "SELECT * FROM user WHERE first_name LIKE :first AND " +
                "last_name LIKE :last LIMIT 1"
    )
    fun findByName(first: String, last: String): User

    @Insert(onConflict = REPLACE)
    suspend fun insertAll(vararg users: User)

    @Insert(onConflict = REPLACE)
    suspend fun insert(vararg users: User)

    @Delete
    fun delete(user: User)

    @Query("DELETE FROM user")
    suspend fun deleteAll()

    @Query("SELECT * FROM user LIMIT :pageSize OFFSET :offset")
    fun getAll(offset: Int, pageSize: Int): Flow<List<User>>

    @Query("SELECT count(*) FROM user")
    suspend fun getSize() : Int
}