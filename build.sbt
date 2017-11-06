import scalariform.formatter.preferences._

name := "play2-mailgun"

val deps = Seq(
    "com.typesafe.play"       		%%  "play-json"             % "2.6.7" 	    % "provided",
		"com.typesafe.play"       		%%  "play-ws"               % "2.6.7" 	    % "provided",
    "org.mockito"                 %   "mockito-all"           % "1.10.19"     % "test",
    "org.specs2"                  %%  "specs2"                % "2.5"		      % "test",
		"commons-fileupload" 					% 	"commons-fileupload" 		% "1.3.3"       % "test",
    "javax.servlet" 							% 	"javax.servlet-api" 		% "4.0.0"       % "test"
)

lazy val commonSettings = Seq(
	organization := "cn.playalot",
	// If the CI supplies a "build.version" environment variable, inject it as the rev part of the version number:
	version := "2.6.7",
	scalaVersion := "2.12.4",
	crossScalaVersions := Seq("2.11.11", "2.12.4"),
	crossVersion := CrossVersion.binary,
	libraryDependencies ++= deps
)

lazy val publishSettings = Seq(
  organization := "cn.playalot",
  publishMavenStyle := true,
  publishArtifact in Test := false,
  publishTo := {
    val nexus = "https://oss.sonatype.org/"
    if (isSnapshot.value)
      Some("snapshots" at nexus + "content/repositories/snapshots")
    else
      Some("releases"  at nexus + "service/local/staging/deploy/maven2")
  },
  licenses += ("MIT", url("http://opensource.org/licenses/MIT")),
  scmInfo := Some(
    ScmInfo(
      url("https://github.com/playalot/play2-mailgun"),
      "scm:git@github.com:playalot/play2-mailgun.git"
    )
  )
)


lazy val root = project.in(file(".")).settings(commonSettings:_*).settings(publishSettings: _*)
lazy val templating = project.in(file("templating")).settings(commonSettings:_*)


resolvers ++= Seq(
  "oss-snapshots" at "https://oss.sonatype.org/content/repositories/snapshots",
  "oss-releases"  at "https://oss.sonatype.org/content/repositories/releases",
  "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/")

publishArtifact in (Compile, packageDoc) := false

developers := List(
  Developer(id="gguan", name="Guan Guan", email="guanguan1114@gmail.com", url=url("http://github.com/gguan"))
)

scalariformPreferences := scalariformPreferences.value
    .setPreference(AlignSingleLineCaseStatements, true)
    .setPreference(DoubleIndentConstructorArguments, true)
    .setPreference(DanglingCloseParenthesis, Preserve)
