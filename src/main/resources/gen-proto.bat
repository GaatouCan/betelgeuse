@echo off
cd ../

set PROTOC=D:\library\install\protobuf\bin\protoc.exe
set PROTO_DIR=.\resources\proto\


for %%F in (%PROTO_DIR%\*) do (
    :: 检查文件扩展名是否为 .proto
    if /i "%%~xF"==".proto" (
        echo %%~nxF
    )
)

call %PROTOC% --proto_path=%PROTO_DIR% --java_out=.\java --kotlin_out=.\kotlin %PROTO_DIR%*.proto

pause