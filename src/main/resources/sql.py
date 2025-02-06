import os
import json
import platform
import re
import sys

VERSION = '0.1'

VARCHAR_LENGTH = 255

# sql类型
sql_type_map = {
    'BIGINT': "long",
    'bigint': "long",
    
    'INT': "integer",
    'int': "integer",

    'SMALLINT': "short",
    'smallint': "short",

    'BIGINT(20)': "long",
    'bigint(20)': "long",
    
    'INT(11)': "integer",
    'int(11)': "integer",

    'SMALLINT(6)': "short",
    'smallint(6)': "short",

    'TINYINT(1)': "bool",
    'tinyint(1)': "bool",

    'BOOLEAN': "bool",
    'boolean': "bool",

    'FLOAT': "float",
    'float': "float",

    'DOUBLE': "double",
    'double': "double",

    'CHAR': "char",
    'char': "char",

    'VARCHAR(255)': "varchar",
    'varchar(255)': "varchar",

    'TEXT': "text",
    'text': "text",

    'BLOB': "blob",
    'blob': "blob",

    'DATETIME': "datetime",
    'datetime': "datetime",

    'TIMESTAMP': "timestamp",
    'timestamp': "timestamp",
}

def to_upper_camel_case(x):
    """转大驼峰法命名"""
    s = re.sub('_([a-zA-Z])', lambda m: (m.group(1).upper()), x)
    return s[0].upper() + s[1:]

def to_lower_camel_case(x):
    """转小驼峰法命名"""
    s = re.sub('_([a-zA-Z])', lambda m: (m.group(1).upper()), x)
    return s[0].lower() + s[1:]

def parse_table_field(line: str) -> dict:
    """解析单行table字段"""
    if line.endswith(','):
        line = line[:-1]

    field_info = {
        "null": True,
        "default": ""
    }
    temp = line.split(' ')

    # 类型解析
    field_info["name"] = temp[0][1:-1]
    field_info["type"] = sql_type_map[temp[1].strip()]

    if temp[2] and (temp[2] == "UNSIGNED" or temp[2] == "unsigned"):
        field_info["type"] = 'u' + field_info["type"]

    for idx, str in enumerate(temp):
        # 默认值
        if str.startswith('DEFAULT'):
            field_info['default'] = temp[idx + 1]

        # 注释
        if str.startswith('COMMENT'):
            field_info['comment'] = temp[idx + 1][1:-1]

        if str.startswith('NOT'):
            if temp[idx + 1] == "NULL":
                field_info['null'] = False

    return field_info


def generate_kotlin_object(src: str, dist: str, desc: str):
    """生成Kotlin Object Relational Mapping文件"""

    assert src, 'sql input path error'
    assert dist, 'orm output path error'
    assert desc, 'desc output path error'

    print(f'-- SQL input path: {os.getcwd()}\\{src}')
    print(f'-- ORM output path: {os.getcwd()}\\{dist}')
    print(f'-- Describe output path: {os.getcwd()}\\{desc}')

    if not os.path.exists(dist):
        os.makedirs(dist)
        print("-- Creating ORM folder")

    sql_list = {}
    table_name_set = set()

    for root, dirs, files in os.walk(src):
        for file in files:
            if not file.endswith('.sql'):
                continue

            table_info = {}
            table_info['field'] = {}
            table_info['origin'] = ""
            table_info['key'] = []

            next = False

            with open(os.path.join(root, file), 'r', encoding='utf-8') as file:
                # sql文件名（无.sql后缀）
                file_name = os.path.basename(file.name)
                file_name, file_extension = os.path.splitext(file_name)
                sql_list[file_name] = []
            
                line = file.readline()
                while line:
                    line = line.strip()

                    # 表名
                    if line.startswith("CREATE TABLE"):
                        if next:
                            if table_info['name'] in table_name_set:
                                raise NameError(f"{table_info['name']} redefined.") 
                    
                            table_name_set.add(table_info['name'])
                            
                            sql_list[file_name].append(table_info)
                            table_info = {}
                            table_info['field'] = {}
                            table_info['origin'] = ""
                            table_info['key'] = []
                        
                        next = True
                    
                        temp = line.split(' ')
                        table_info['name'] = temp[2][1:-1]
                        table_info['origin'] += line
                        if not line.endswith("("):
                            table_info['origin'] += " ("

                    # 字段
                    if line.startswith('`'):
                        table_info['origin'] += line
                        field_info = parse_table_field(line)
                        table_info['field'][field_info['name']] = field_info

                    # 键
                    if line.startswith("PRIMARY KEY"):
                        table_info['origin'] += line
                        position = line.index('(')
                        line = line[position + 1:-1]

                        temp = line.split(',')

                        for key in temp:
                            table_info['field'][key.strip()[1:-1]]['key'] = True
                            table_info['key'].append(key.strip()[1:-1])

                    # 表定义结束
                    if line.startswith(')'):
                        table_info['origin'] += line
                        line = line[1:]
                        if line.endswith(';'):
                            line = line[:-1]

                        temp = line.split(' ')
                        for idx, str in enumerate(temp):
                            if str == 'COMMENT':
                                table_info['comment'] = temp[idx+1][1:-1]

                    line = file.readline()   

                if next:
                    if table_info['name'] in table_name_set:
                        raise NameError(f"{table_info['name']} redefined.") 
                    
                    table_name_set.add(table_info['name'])
                    sql_list[file_name].append(table_info)

                print(f"-- \t{file.name} loaded")    

    # 生成JSON数据文件
    with open(desc, 'w', encoding='utf-8') as file:
        file.write(json.dumps(sql_list, indent=4, ensure_ascii=False))

    
    table_count = 0
    for file_name, table_list in sql_list.items():

        for table in table_list:
            table_name = to_upper_camel_case(table['name'])

            # 定义头文件
            with open(os.path.join(dist, table_name + 'Table.kt'), 'w', encoding='utf-8') as file:

                file.write(f'''/**
 * Object Relational Mapping Kotlin object definition
 * This file is generated by Python script. Do not edit!!!
 * Python version: v{platform.python_version()}
 * Script version: v{VERSION}
 * Source file: /sql/{file_name}
 */\n\n''')
            
                file.write('package org.example.table\n\n')
                file.write('import org.jetbrains.exposed.sql.Table\n')
                file.write('import org.jetbrains.exposed.sql.javatime.*\n\n')

                file.write('object %sTable : Table() {\n' % table_name)

                for field in table['field'].values():

                    if field['type'] == "varchar":
                        file.write(f'\tval {to_lower_camel_case(field["name"])} = varchar("{field["name"]}", {VARCHAR_LENGTH})')
                    else:
                        file.write(f'\tval {to_lower_camel_case(field["name"])} = {field['type']}("{field["name"]}")')

                    if 'default' in field.keys() and field['default'] != "":
                        if field['type'] == 'bool':
                            if field['default'] == '1' or field['default'] == 'TRUE':
                                file.write('.default(true)')
                            elif field['default'] == '0' or field['default'] == 'FALSE':
                                file.write('.default(false)')
                        elif field['type'] == 'datetime' or field['type'] == 'timestamp':
                            file.write(f".default(org.jetbrains.exposed.sql.StdDateTime.now())")
                        else:
                            file.write(f".default({field['default']})")

                    file.write('\n')

                primary_key = ', '.join([f'{to_lower_camel_case(key)}' for key in table['key']])
                file.write(f'\toverride val primaryKey = PrimaryKey({primary_key})\n')
                
                file.write('}\n')

                table_count += 1
        
    print(f"-- \t{table_count} table(s) completed")

def main(argv):
    SQL_DIR = 'sql'
    ORM_DIR = 'table'
    DESC_FILE = 'desc.json'

    for arg in argv:
        if arg.startswith('--sql='):
            SQL_DIR = arg[6:]
        if arg.startswith('--orm='):
            ORM_DIR = arg[6:]
        if arg.startswith('--desc='):
            DESC_FILE = arg[7:]
                
    generate_kotlin_object(SQL_DIR, ORM_DIR, DESC_FILE)

if __name__ == "__main__":
    main(sys.argv)