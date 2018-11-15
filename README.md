# Mapper library
## Download
Use Gradle:
```
dependencies {
    implementation 'com.ivanovrb.mapper:mapper:1.1.3'
    kapt 'com.ivanovrb.mapper:mapperCompiler:1.1.3'
}
```
## Usage
Mapper is library for mapping data classes
### @Mapper
For indiacte mapping class you must use **@Mapper** annotation
```
data class User(
        val id:Int = 0,
        val fullName:String = ""
)

@Mapper(User::class)
data class UserDto(
        val id: Int? = 0,
        val fullName: String? = ""
)
```
After building project you will can use extension **User().mapToUserDto()**

### @IgnoreMap
For ignore specefic fields use **@IgnoreMap**, but you must define defaul value
```
data class User(
        val id:Int,
        val fullName:String,
        @IgnoreMap val isAuthor:Boolean = false
)

@Mapper(User::class)
data class UserDto(
        val id:Int?=0,
        val fullName:String?=""
)
```
### @MappingName
If you want use different fields use **@MappingName** annotation
```
data class User(
        val id:Int,
        val fullName:String
)

@Mapper(User::class)
data class UserDto(
        val id:Int?=0,
        @MappingName("fullName") val name:String?=""
)
```
### @Default
If class has nullable primitive types library will use default values, but you can change it using **@Default** annotation. Unfortunately, annotation require string value
```
@Mapper(User::class)
data class UserDto(
        val id:Int?=0,
        @Default("Full Name") val fullName:String?=""
)
```
### @MappingConstructor
If you have more then one constructor library will choose the most suitable one, also you can also specify the constructor itself with the annotation **@MappingConstructor**
```
@Mapper(User::class)
class UserDto(
        val id: Int = 1,
        val fullName: String = "vswer",
        @IgnoreMap val ignore: Int = 123
) {
    @MappingConstructor constructor(
            id: Int?,
            fullName: String?
    ) : this(id ?: 0, fullName ?: "")
}
```
