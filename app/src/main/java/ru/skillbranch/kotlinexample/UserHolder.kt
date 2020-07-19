package ru.skillbranch.kotlinexample

import androidx.annotation.VisibleForTesting
import kotlin.math.log

object UserHolder {
    private val map = mutableMapOf<String, User>()

    fun registerUser(
        fullName: String,
        email: String,
        password:String
    ): User{
        val user = User.makeUser(fullName, email=email, password = password)
        if (map.containsKey(user.login)) throw IllegalArgumentException("A user with this email already exists")
        map[user.login] = user
        return user
//        return User.makeUser(fullName, email=email, password = password)
//            .also { user -> map[user.login] = user }
    }

    fun registerUserByPhone(fullName: String, rawPhone: String): User{
        val regex = Regex(pattern = "[A-Za-zА-Яа-я]")
        val regex2 = Regex(pattern = "^[^+]")
        val _phone = rawPhone.replace("""[^\d]""".toRegex(), "")
        if (regex.containsMatchIn(input = rawPhone) || regex2.containsMatchIn(input = rawPhone) || _phone.length != 11){
            throw IllegalArgumentException("Enter a valid phone number starting with a + and containing 11 digits")
        }
        val user = User.makeUser(fullName, phone = rawPhone)
        if (map.containsKey(user.login)) throw IllegalArgumentException("A user with this phone already exists")
        map[user.login] = user
        return user
    }
    fun loginUser(login:String, password: String) : String?{
        val regexp = Regex(pattern = "@")
        val a:String
        a = if (regexp.containsMatchIn(input = login)){
            login.trim()
        }else{
            login.replace("""[^+\d]""".toRegex(), "")
        }

        return map[a]?.run {
            if (checkPassword(password)) this.userInfo
            else null

        }
    }

    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    fun clearHolder(){
        map.clear()
    }

    fun requestAccessCode(login: String) : Unit{
        val a = login.replace("""[^+\d]""".toRegex(), "")
        print(a)
        val user = map[a]
        print(map)
        val code = user!!.generateAccessCode()
        user.passwordHash = user.encrypt(code)
        user.accessCode = code
//        user.sendAccessCodeToUser(user.phone, code)
    }

}