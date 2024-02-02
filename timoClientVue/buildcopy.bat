cd C:\IntelliJ\wise23-24_grpl_shrimps\src\main\resources\static\assets
del *.js /s /Q
del *.css /s /Q
cd..
del index.html /Q
del favicon.ico /Q
cd C:\IntelliJ\ClientVue\timoClientVue\dist
xcopy *.* C:\IntelliJ\wise23-24_grpl_shrimps\src\main\resources\static /F /E /Y
cd C:\IntelliJ\ClientVue\timoClientVue\
xcopy *.* C:\IntelliJ\wise23-24_grpl_shrimps\timoClientVue /F /E /Y