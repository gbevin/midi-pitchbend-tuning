package com.uwyn.midi;

import rife.bld.Project;

import java.util.List;

import static rife.bld.dependencies.Repository.*;
import static rife.bld.dependencies.Scope.*;

public class PitchBendTablesBuild extends Project {
    public PitchBendTablesBuild() {
        pkg = "com.uwyn.midi";
        name = "PitchBendTables";
        mainClass = "com.uwyn.midi.PitchBendTables";
        version = version(0,1,0);

        downloadSources = true;
        repositories = List.of(MAVEN_CENTRAL, RIFE2_RELEASES);
        autoDownloadPurge = true;

        scope(compile)
            .include(dependency("com.uwyn.rife2", "rife2", version(1,7,3)));
        scope(test)
            .include(dependency("org.junit.jupiter", "junit-jupiter", version(5,10,0)))
            .include(dependency("org.junit.platform", "junit-platform-console-standalone", version(1,10,0)));
    }

    public static void main(String[] args) {
        new PitchBendTablesBuild().start(args);
    }
}