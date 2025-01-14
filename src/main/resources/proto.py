import platform
import shutil
import os
import sys
import re
import subprocess

VERSION = '0.1'

PROTO_LIST = [
    'player',
    'friend'
]

def camel_to_snake(name):
    s1 = re.sub('(.)([A-Z][a-z]+)', r'\1_\2', name)
    return re.sub('([a-z0-9])([A-Z])', r'\1_\2', s1).upper()
        
def main(argv):

    DEFINE_DIR = "proto"
    OUTPUT_DIR = "../kotlin/controller"
    # OUTPUT_DIR = "controller"
    GROUP_NAME = "org.example.controller"

    PROTOC = "D:\\library\\install\\protobuf\\bin\\protoc.exe"
    PROTO_OUTPUT = "../"

    for arg in argv:
        if arg.startswith('--define='):
            DEFINE_DIR = arg[9:]
        elif arg.startswith('--output='):
            OUTPUT_DIR = arg[9:]

    assert DEFINE_DIR, 'proto define path error'
    assert OUTPUT_DIR, 'proto output path error'

    print(f'-- Protobuf input path: {os.getcwd()}\\{DEFINE_DIR}')
    print(f'-- Protobuf output path: {os.getcwd()}\\{OUTPUT_DIR}')

    if not os.path.exists(OUTPUT_DIR):
        os.makedirs(OUTPUT_DIR)
        print("-- Creating proto output folder")

    if os.path.exists(PROTO_OUTPUT + '/java'):
        shutil.rmtree(PROTO_OUTPUT + '/java')
    os.makedirs(PROTO_OUTPUT + '/java')
        
    if os.path.exists(PROTO_OUTPUT + '/kotlin'):
        if os.path.exists(PROTO_OUTPUT + '/kotlin/proto'):
            shutil.rmtree(PROTO_OUTPUT + '/kotlin/proto')
    else:
        os.makedirs(PROTO_OUTPUT + '/kotlin')

    proto_data = []
    proto_name_set = []

    for val in PROTO_LIST:
        with open(os.path.join(DEFINE_DIR, val + '.proto'), 'r', encoding='utf-8') as file:
            package = {'list': []}
                
            line = file.readline()
            while line:
                line = line.strip()

                if line.startswith('package'):
                    package['package'] = line[8:-1].strip()

                if line.startswith('message'):
                    line = line[8:-1].strip()

                    if line.endswith('_'):
                        line = file.readline()
                        continue

                    if line in proto_name_set:
                        raise f"{line} redefined."

                    proto_name_set.append(line)
                    package['list'].append(line)

                line = file.readline()

            proto_data.append(package)

        print(f"-- {file.name} have done")
    
    # print(proto_data)

    with open(os.path.join(OUTPUT_DIR, 'ProtocolType.kt'), 'w', encoding='utf-8') as file:
        file.write(f'''/**
 * Protocol ID define here by enum class
 * This file is generated by Python script. Do not edit!!!
 * Python version: v{platform.python_version()}
 * Script version: v{VERSION}
 */\n\n''')
        
        file.write(f'package {GROUP_NAME}\n\n')
        file.write('enum class ProtocolType(val value: Int) {\n')

        package_index = 0
        proto_index = 1

        for package in proto_data:
            
            package_index += 1
            proto_index = 1
            
            file.write(f'\t// {package['package']}\n')

            for proto in package['list']:
                pkg_id = package_index * 1000 + proto_index
                file.write(f"\t{camel_to_snake(proto)}({pkg_id}),\n")

                proto_index += 1

            file.write('\n')

        file.write(f'\tPROTO_TYPE_MAX({package_index * 1000 + proto_index}),\n')
        file.write('}\n')

    command = f'{PROTOC} --proto_path={DEFINE_DIR} --java_out={PROTO_OUTPUT}/java --kotlin_out={PROTO_OUTPUT}/kotlin {DEFINE_DIR}/*.proto'
    result = subprocess.run(command, shell=True, capture_output=True, text=True)
    
    print(result.stdout)
    print('-- Protobuf compile done')

if __name__ == "__main__":
    main(sys.argv)