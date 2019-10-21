
plugins {
	java
    `java-library`
}

group = "truenotzero"
version = "1.0-SNAPSHOT"

java {
	sourceCompatibility = JavaVersion.VERSION_1_8
	targetCompatibility = JavaVersion.VERSION_1_8
}

repositories {
	jcenter()
}

dependencies {
	implementation(project(":api"))

	testImplementation("junit:junit:4.12")
	testImplementation("org.mockito:mockito-core:3.1.0")

    implementation("org.reflections:reflections:0.9.11")
    implementation("com.google.code.gson:gson:2.8.2")
}