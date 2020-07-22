package ru.skillbranch.kotlinexample

import androidx.annotation.VisibleForTesting
import kotlin.math.log

object UserHolder {
    private val map = mutableMapOf<String, User>()

    fun registerUser(
        fullName: String,
        email: String,
        password:String,
        salt: String? = null,
        hash: String? = null
    ): User{
        val user = User.makeUser(fullName, salt = salt, hash = hash, email=email, password = password)
        if (map.containsKey(user.login)) throw IllegalArgumentException("A user with this email already exists")
        map[user.login] = user
        return user
//        return User.makeUser(fullName, email=email, password = password)
//            .also { user -> map[user.login] = user }
    }

    fun registerUserByPhone(fullName: String, rawPhone: String, salt: String? = null, hash: String? = null): User{
        val regex = Regex(pattern = "[A-Za-zА-Яа-я]")
        val regex2 = Regex(pattern = "^[^+]")
        val _phone = rawPhone.replace("""[^\d]""".toRegex(), "")
        if (regex.containsMatchIn(input = rawPhone) || regex2.containsMatchIn(input = rawPhone) || _phone.length != 11){
            throw IllegalArgumentException("Enter a valid phone number starting with a + and containing 11 digits")
        }
        println(fullName)
        println(rawPhone)
        val user = User.makeUser(fullName, salt = salt, hash = hash, phone = rawPhone)
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
        println(a)
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
    fun importUsers(list: List<String>): List<User> {
        // Полное имя пользователя; email; соль:хеш пароля; телефон (Пример: " John Doe ;JohnDoe@unknow.com;[B@7591083d:c6adb4becdc64e92857e1e2a0fd6af84;;")
        var attrUser: List<String>
        var users: MutableList<User> = mutableListOf()
        var user: User
        for (i in list.indices){
            attrUser = list[i].split(";")
            try {
                if (!attrUser[1].isNullOrBlank()){
                    user = registerUser(attrUser[0], attrUser[1],"1234", attrUser[2].split(":")[0], attrUser[2].split(":")[1])
                        .also { users.add(it) }
                }else if (!attrUser[3].isNullOrBlank()){
                    user = registerUserByPhone(attrUser[0], attrUser[3], attrUser[2].split(":")[0], attrUser[2].split(":")[1])
                        .also { users.add(it) }
                }
            }catch (e: Exception){

            }
        }
        return users
    }

}