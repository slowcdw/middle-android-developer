package ru.skillbranch.kotlinexample

import org.junit.After
import org.junit.Assert
import org.junit.Test
import ru.skillbranch.kotlinexample.extentions.dropLastUntil
import java.math.BigInteger
import java.security.MessageDigest
import java.security.SecureRandom

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {

    /**
    Добавьте метод в UserHolder для очистки значений UserHolder после выполнения каждого теста,
    это необходимо чтобы тесты можно было запускать одновременно

    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    fun clearHolder(){
    map.clear()
    }
     */
    @After
    fun after(){
        UserHolder.clearHolder()
    }

    @Test
    fun register_user_success() {
        val holder = UserHolder
        val user = holder.registerUser("John Doe", "John_Doe@unknown.com","testPass")
        val expectedInfo = """
            firstName: John
            lastName: Doe
            login: john_doe@unknown.com
            fullName: John Doe
            initials: J D
            email: John_Doe@unknown.com
            phone: null
            meta: {auth=password}
        """.trimIndent()

        Assert.assertEquals(expectedInfo, user.userInfo)
    }

    @Test(expected = IllegalArgumentException::class)
    fun register_user_fail_blank() {
        val holder = UserHolder
        holder.registerUser("", "John_Doe@unknown.com","testPass")
    }

    @Test(expected = IllegalArgumentException::class)
    fun register_user_fail_illegal_name() {
        val holder = UserHolder
        holder.registerUser("John Jr Doe", "John_Doe@unknown.com","testPass")
    }

/*    @Test
    fun qwe(){
        val map = mutableMapOf<String, Int>()
        val email: String
        email = "cvbnm"
        map[email] = 4567
        map["qwe"] = 1
        map["zxc"] = 2
        if (map.containsKey(email)) print("!!!")
        print(map)

    }*/

    @Test(expected = IllegalArgumentException::class)
    fun register_user_fail_illegal_exist() {
        val holder = UserHolder
        holder.registerUser("John Doe", "John_Doe@unknown.com","testPass")
        holder.registerUser("John Doe", "John_Doe@unknown.com","testPass")
    }

    @Test
    fun register_user_by_phone_success() {
        val holder = UserHolder
        val user = holder.registerUserByPhone("John Doe", "+7 (917) 971 11-11")
        val expectedInfo = """
            firstName: John
            lastName: Doe
            login: +79179711111
            fullName: John Doe
            initials: J D
            email: null
            phone: +79179711111
            meta: {auth=sms}
        """.trimIndent()

        Assert.assertEquals(expectedInfo, user.userInfo)
        Assert.assertNotNull(user.accessCode)
        Assert.assertEquals(6, user.accessCode?.length)
    }

    @Test(expected = IllegalArgumentException::class)
    fun register_user_by_phone_fail_blank() {
        val holder = UserHolder
        holder.registerUserByPhone("", "+7 (917) 971 11-11")
    }

 /*   @Test
    fun tel() {
        val tel = "+7 (XXX) XX XX-XX"
        val regex = Regex(pattern = "[A-Za-zА-Яа-я]")
        val regex2 = Regex(pattern = "^[^+]")
        val _phone = tel.replace("""[^\d]""".toRegex(), "")

        if (regex.containsMatchIn(input = tel) || regex2.containsMatchIn(input = tel) || _phone.length != 11){
            throw IllegalArgumentException("Enter a valid phone number starting with a + and containing 11 digits")
        }
        print(_phone.length)
        print(0)
    }*/

    @Test(expected = IllegalArgumentException::class)
    fun register_user_by_phone_fail_illegal_name() {
        val holder = UserHolder
        holder.registerUserByPhone("John Doe", "+7 (XXX) XX XX-XX")
    }

    @Test(expected = IllegalArgumentException::class)
    fun register_user_failby_phone_illegal_exist() {
        val holder = UserHolder
        holder.registerUserByPhone("John Doe", "+7 (917) 971-11-11")
        holder.registerUserByPhone("John Doe", "+7 (917) 971-11-11")
    }

    @Test
    fun login_user_success() {
        val holder = UserHolder
        holder.registerUser("John Doe", "John_Doe@unknown.com","testPass")
        val expectedInfo = """
            firstName: John
            lastName: Doe
            login: john_doe@unknown.com
            fullName: John Doe
            initials: J D
            email: John_Doe@unknown.com
            phone: null
            meta: {auth=password}
        """.trimIndent()

        val successResult =  holder.loginUser("john_doe@unknown.com", "testPass")

        Assert.assertEquals(expectedInfo, successResult)
    }

    @Test
    fun login_user_by_phone_success() {
        val holder = UserHolder
        val user = holder.registerUserByPhone("John Doe", "+7 (917) 971-11-11")
        val expectedInfo = """
            firstName: John
            lastName: Doe
            login: +79179711111
            fullName: John Doe
            initials: J D
            email: null
            phone: +79179711111
            meta: {auth=sms}
        """.trimIndent()

        val successResult =  holder.loginUser("+7 (917) 971-11-11", user.accessCode!!)

        Assert.assertEquals(expectedInfo, successResult)
    }

    @Test
    fun login_user_fail() {
        val holder = UserHolder
        holder.registerUser("John Doe", "John_Doe@unknown.com","testPass")

        val failResult =  holder.loginUser("john_doe@unknown.com", "test")

        Assert.assertNull(failResult)
    }

    @Test
    fun login_user_not_found() {
        val holder = UserHolder
        holder.registerUser("John Doe", "John_Doe@unknown.com","testPass")

        val failResult =  holder.loginUser("john_cena@unknown.com", "test")

        Assert.assertNull(failResult)
    }

    @Test
    fun request_access_code() {
        val holder = UserHolder
        val user = holder.registerUserByPhone("John Doe", "+7 (917) 971-11-11")
        val oldAccess = user.accessCode
        holder.requestAccessCode("+7 (917) 971-11-11")

        val expectedInfo = """
            firstName: John
            lastName: Doe
            login: +79179711111
            fullName: John Doe
            initials: J D
            email: null
            phone: +79179711111
            meta: {auth=sms}
        """.trimIndent()

        val successResult =  holder.loginUser("+7 (917) 971-11-11", user.accessCode!!)

        Assert.assertNotEquals(oldAccess, user.accessCode!!)
        Assert.assertEquals(expectedInfo, successResult)
    }
    class CsvTest {
        @Test
        fun parse_csv_users() {
            val users = listOf(
                " John Doe ;JohnDoe@unknow.com;[B@7fbe847c:91a3c589fd7bd0861d06b023bdaebe1c;;",
                "John Doe;JohnDoe@unknow.com;[B@7fbe847c:91a3c589fd7bd0861d06b023bdaebe1c;;",
                "John;JohnDoe@unknow.com;[B@7fbe847c:91a3c589fd7bd0861d06b023bdaebe1c;;",
                " John Doe ;JohnDoe@unknow.com;[B@7fbe847c:91a3c589fd7bd0861d06b023bdaebe1c;+7 (917) 971 11-11;",
                " John Doe ;;[B@7fbe847c:91a3c589fd7bd0861d06b023bdaebe1c;+7 (917) 971 11-11;"
            )
            UserHolder.importUsers(users).forEach {
                println(UserHolder.loginUser(it.login, "123456"))
                Assert.assertNotNull(UserHolder.loginUser(it.login, "123456"))
            }

        }

        @Test
        fun md5() {
            val salt = salt()
            println(salt)
            println(salt.plus("123456").md5())
        }

        private fun salt() = ByteArray(16).also { SecureRandom().nextBytes(it) }.toString()

        private fun String.md5(): String {
            val md = MessageDigest.getInstance("MD5")
            val digest = md.digest(toByteArray())
            val hexString = BigInteger(1, digest).toString(16)
            return hexString.padStart(32, '0')
        }
    }

    @Test
    fun extList(){
        var a = mutableListOf(1,2,3,4,5,6,7,8)
        Assert.assertEquals(mutableListOf(1 ,2,3), a.dropLastUntil{it == 4})
    }

}
