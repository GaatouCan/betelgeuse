import sys
import os
import pandas
import json

def main(argv):
    XLSX_DIR = 'config/xlsx'
    JSON_DIR = 'config/json'

    for arg in argv:
        if arg.startswith('--xlsx='):
            XLSX_DIR = arg[7:]
        elif arg.startswith('--json='):
            JSON_DIR = arg[7:]

    assert XLSX_DIR, 'Xlsx input path error'
    assert JSON_DIR, 'Json output path error'

    print(f'-- Xlsx input path: {os.getcwd()}\\{XLSX_DIR}')
    print(f'-- JSON output path: {os.getcwd()}\\{JSON_DIR}')

    if not os.path.exists(JSON_DIR):
        os.makedirs(JSON_DIR)
        print('-- Creating Json output floder')

    count = 0
    for root, dirs, files in os.walk(XLSX_DIR):
        for file in files:
            if not file.endswith('.xlsx'):
                continue

            if file.startswith('~'):
                continue

            file_path = os.path.join(root, file)
            df = pandas.read_excel(file_path)
            print(f'-- Loaded {file_path}')

            df = df.drop([0])

            columns = []
            output_data = {}

            row_by_iloc = df.iloc[0]

            for col in df.columns:
                if 's' in row_by_iloc[col]:
                    columns.append(col)

            for idx, row in df.iterrows():
                if idx <= 1:
                    continue

                item = {}
                for f in columns:
                    item[f] = row[f]

                output_data[item["id"]] = item

            json_dir = root.replace(XLSX_DIR, JSON_DIR)
            json_file = os.path.join(json_dir, file[:-5] + '.json')

            if not os.path.exists(json_dir):
                os.makedirs(json_dir)

            with open(json_file, 'w', encoding='utf-8') as file:
                file.write(json.dumps(output_data, indent=4, ensure_ascii=False))
            
            # print(f'-- Generated {json_file}')

            # df.to_csv('%s/%s.csv' % (dist_dir, file[:-5]), index=False)
            count += 1

    print(f'-- {count} files has done')

if __name__ == '__main__':
    main(sys.argv)