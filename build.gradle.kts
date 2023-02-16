plugins {
    id("application")
}

group = "com.uwyn.midi"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.uwyn.rife2:rife2:1.2.1")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.1")
}

application {
    mainClass.set("com.uwyn.midi.PitchBendTables")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}