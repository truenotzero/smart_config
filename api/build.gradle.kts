
plugins {
	java
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
	testImplementation("junit:junit:4.12")
	testImplementation("org.mockito:mockito-core:3.1.0")
}