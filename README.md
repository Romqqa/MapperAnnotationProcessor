# Mapper library
## Download
Use Gradle:
```
dependencies {
  implementation 'com.ivanovrb.mapper:mapper:1.1.1'
  kapt 'com.ivanovrb.mapper:mapperCompiler:1.1.1'
}
```
## Usage
Mapper is library for mapping data classes
### @Mapper
For indiacte mapping class you must use **@Mapper** annotation
```
data class User(
  val id:Int,
  val fullName:String
)

@Mapper(User::class)
data class UserDto(
  val id:Int?=0,
  val fullName:String?=""
)
```
After building project you will can use extension **User().mapToUserMap()**

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
  @MappingName("fullName") val full_name:String?=""
)
```
### @Default
If class has nullable primitive types library will use default values, but you can change it using **@Default** annotation. Unfortunately,
annotation require string value
```
@Mapper(User::class)
data class UserDto(
  val id:Int?=0,
  @Default("Full Name") val fullName:String?=""
)
```
