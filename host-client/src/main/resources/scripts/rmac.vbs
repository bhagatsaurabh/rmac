CreateObject("Wscript.Shell").Run """${JRE_LOCATION}\bin\java"" -jar ""${CURRENT_LOCATION}\RMACClient.jar"" ""${RUNTIME_LOCATION}""", 0, False
CreateObject("Wscript.Shell").Run """${JRE_LOCATION}\bin\java"" -jar ""${CURRENT_LOCATION}\RMACUpdater.jar"" ""${RUNTIME_LOCATION}""", 0, False
