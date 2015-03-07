# -*- coding: utf-8 -*-
# <nbformat>3.0</nbformat>

# <markdowncell>

# # Schedule Table Structure #
#     0. Flg
#     1. Class Number
#     2. Subject
#     3. Catalog Number
#     4. Section
#     5. Component
#     6. Units
#     7. Title
#     8. Days Taught
#     9. Time
#     10. Location
#     11. Class Attributes
#     12. Instructor
#     13. Feed Back
#     14. Pre Req
#     15. Fees
# 
# ### Interesting Parts ###
#     1. Class/Index Number
#     3. (CS +) Catalog Number !
#     4. Section
#     7. Title
#     8. Days Taught
#     9. Time
#     10. Location !
#     12. Instructor !
# 
# # Seating/Enrollment Table Structure #
#     0. Class (Index) Number
#     1. Subject
#     2. Catalog Number
#     3. Section
#     4. Title
#     5. Enrollment Cap
#     6. Currently Enrolled
#     7. Seats Available
# 
# ### Interesting Parts ###
#     0. Class (Index) Number (Not anymore!! Some classes don't have an index number in the schedule)
#     2. Catalog Number
#     3. Section (Primary key: Catalog Number + Section)
#     5. Enrollment Cap
#     6. Currently Enrolled
#     7. Seats Available

# <markdowncell>

# ### Import libraries, setup variables

# <codecell>

import requests, bs4
import re, json

schedule_url = r'http://www.acs.utah.edu/uofu/stu/scheduling?term=1154&dept=CS&classtype=g'
enrollment_url = r'http://www.acs.utah.edu/uofu/stu/scheduling/crse-info?term=1154&subj=CS'

api_key = '0123456789'
schedule_name = 'Spr15 CS Schedule'
OUT_FILE = '/home/dttvinh/new_spr15_schedule.json'

keep_all_data = True

CNAME = 'name'
CID = 'id'
CDAYS = 'days_count'
CDURATION = 'duration'
CSTARTTM = 'pStartTm'
CSPACEID = 'spaceId'
CPERSON = 'persons'
CMAXPTCPNT = 'max_participants'

# <codecell>

CLASS_DICT = {'index':1, 'catalog_number':3, 'section':4, 'title':7, 'days': 8, 'time': 9, 'location':10, 'instructor': 12}
#a_link = {class_dict[item] for item in ['catalog_number', 'location', 'instructor']}

ENROLLMENT_DICT = {'index':0, 'catalog_number':2, 'section':3, 'enrollment_cap':5, 'enrolled':6}

# <markdowncell>

# ### Helper functions for fetching/parsing html data

# <codecell>

def get_soup(url):
    r = requests.get(url)
    return bs4.BeautifulSoup(r.text)

def get_rowspan(tr):
    return int(tr.td.get('rowspan', '1'))

def skip_rows(tr_iter, count=1):
    while count > 0:
        tr = next(tr_iter)
        count -= get_rowspan(tr)

def get_cell_string(td):
    if td.a != None:
        value = td.a.string
    else:
        value = td.string
    return unicode(value).encode(errors='ignore').strip()

# <markdowncell>

# ### Classes scraper methods

# <codecell>

def get_class_record(tr):
    cells = tr.find_all('td', recursive=False)
    return {key:get_cell_string(cells[index]) for key, index in CLASS_DICT.iteritems()}

def get_class_records(rows):
    return [get_class_record(tr) for tr in rows]

# <codecell>

def get_class_html_data(soup):
    tables = soup.find(class_='sizer').find_all('table', recursive=False)

    table = tables[1]
    rows = table.find_all('tr', recursive=False)
    
    it = iter(rows)
    while True:
        tr = it.next()
        # Ignore "table header" and "row separator"
        if tr.td.string == 'Flg' or len(tr.find_all('td', recursive=False)) == 1:
            continue
        rowspan = get_rowspan(tr)
        yield tr
        skip_rows(it, rowspan - 1)

# <markdowncell>

# ### Enrollment data scraper methods

# <codecell>

def get_enrollment_html_data(soup):
    rows = soup.html.find(id='innerwrapper').table.find_all('tr', recursive=False)
    return rows[1::2]

def get_enrollment_record(tr):
    cells = tr.find_all('td', recursive=False)
    return {key:get_cell_string(cells[index]) for key, index in ENROLLMENT_DICT.iteritems()}

def get_enrollment_records(html_data):
    return [get_enrollment_record(tr) for tr in html_data]

# <markdowncell>

# ### Helper methods for joining classes list and enrollment list

# <codecell>

def get_mapping(item_list, *keys):
    return {tuple(item[key] for key in keys):item for item in item_list}

def join_tables(main_list, second_list, *joint_keys):
    lookup_mapping = get_mapping(second_list, *joint_keys)
    for i in range(len(main_list)):
        lookup_value = tuple(main_list[i][key] for key in joint_keys)
        try:
            main_list[i].update(lookup_mapping[lookup_value])
        except:
            print(main_list[i])
            print(lookup_mapping[lookup_value])
            return

# <markdowncell>

# ### Scraping actual data

# <codecell>

schedule_soup = get_soup(schedule_url)
class_html_data = list(get_class_html_data(schedule_soup))

len(class_html_data)

# <codecell>

class_records = get_class_records(class_html_data)
class_records[100]

# <codecell>

enrollment_soup = get_soup(enrollment_url)

enrollment_html_data = get_enrollment_html_data(enrollment_soup)

enrollment_records = get_enrollment_records(enrollment_html_data)

enrollment_records[0]

# <codecell>

join_tables(class_records, enrollment_records, 'catalog_number', 'section')

# <markdowncell>

# ### Remove classes with TBA day/time/location

# <codecell>

tba_field_func = lambda record, *fields: any([record[field].startswith('TBA') for field in fields])
tba_record_func = lambda d: tba_field_func(d, 'days', 'time', 'location')

final_map = [record for record in class_records if not tba_record_func(record)]

print any(map(tba_record_func, class_records))
print sum(map(tba_record_func, class_records))

print len(class_records)
print len(class_records) - sum(map(tba_record_func, class_records))

print any(map(lambda d: d['days'] == 'TBA', final_map))
print len(final_map)

print(final_map[0])

# <markdowncell>

# ### Helper methods for normalizing classes data/converting to json
# - convert_time: parse a time string to int, used to calculate a class' duration
# - normalize_locations: "Guess" a location's capacity by the highest enrollment-cap value
# - get_possible_start_times: Add & populate pStartTm field to the input dict
# - normalize_class_record: return a dict with necessary fields for the json output (id, name, days_count, ...)
# - filter_records: Filter out records/classes with "unusual" time block (those that we can't handle yet)

# <codecell>

RE_DAYS = re.compile('[MTWHFSU]', flags=re.IGNORECASE)
RE_TIME = re.compile('(\d+):(\d+)\s*(am|pm)', re.IGNORECASE)

# <codecell>

filter_record_func = lambda c: (c['duration'], c['days_count']) in [(50, 1), (80, 1), (50, 2), (80, 2), (50, 3)]

def convert_time(time_tuple):
    h = int(time_tuple[0]) % 12
    m = int(time_tuple[1])
    pm = (time_tuple[2].lower() == 'pm')
    return h * 60 + m + pm * 60 * 12

def normalize_locations(records):
    
    tmp = dict()
    for record in records:
        if tmp.get(record['location'], 0) < int(record['enrollment_cap']):
            tmp[record['location']] = int(record['enrollment_cap'])
    locations = [{'type':'room', 'name':location, 'capacity':capacity,
                  #'time':[[i, 0, 23*60] for i in range(5)]
                  } for location, capacity in tmp.iteritems()]
    n = len(locations)
    for i in range(n):
        locations[i]['id'] = i
    return locations

def get_possible_start_times(record):
    mw = ['0805', '1150', '1325', '1500']
    tth = ['0730', '0910', '1045', '1225', '1400', '1540']
    mwf = ['0730', '0835', '0940', '1045', '1150', '1255', '1400', '1505', '1610']
    block_mapping = {(80,1): [(mw, 'M'), (mw, 'W'), (tth, 'T'), (tth, 'H')],
                     (50, 1): [(mwf, 'M'), (mwf, 'W'), (mwf, 'F')],
                     (80,2): [(mw, 'MW'), (tth, 'TH')],
                     (50,2): [(mwf, 'MW')], (50,3): [(mwf, 'MWF')]}
    
    mapping = block_mapping[(record['duration'], record['days_count'])]
    pStartTm = dict()
    for blocks,day in mapping:
        #pStartTm.extend([day + b for b in blocks])
        pStartTm[day] = blocks
    record['pStartTm'] = pStartTm

def normalize_class_record(record):
    normalize_class_record.id += 1
    output = dict()
    time_start, time_end = RE_TIME.findall(record['time'])
    
    output['name'] = 'CS {}-{}'.format(record['catalog_number'], record['section'])
    output['id'] = normalize_class_record.id
    output['days_count'] = len(RE_DAYS.findall(record['days']))
    output['duration'] = convert_time(time_end) - convert_time(time_start)
    output['max_participants'] = int(record['enrollment_cap'])
    output['persons'] = record['instructor']
    
    return output
normalize_class_record.id = 0

def get_output_json(records):
    normalized_class_data = [normalize_class_record(record) for record in records]
    
    filtered_classes = [record for record in normalized_class_data if filter_record_func(record)]
    
    locations = normalize_locations(records)
    persons = list({record['persons'] for record in filtered_classes})
    persons_mapping = {persons[i]: i for i in range(len(persons))}
    for record in filtered_classes:
        get_possible_start_times(record)
        record['persons'] = persons_mapping[record['persons']]
    
    output_dict = {'name':schedule_name, 'api-key':api_key,
                   'SPACE':locations, 'EVENT':filtered_classes}
    return json.dumps(output_dict, indent=4, sort_keys=True)

# <markdowncell>

# ### Write actual data (in json format) to OUT_FILE

# <codecell>

with open(OUT_FILE, 'w') as f:
    f.write(get_output_json(final_map))

# <markdowncell>

# ### Experimental Area

# <codecell>

header = ['name', 'section', 'days_count', 'duration', 'max_enrollment', 'location', 'professor']

def write_header(f):
    f.write('\t'.join(header))
    f.write('\n')

def write_record(f, output_record):
    f.write('\t'.join([str(output_record[field]) for field in header]))
    f.write('\n')

def write_records(file_name, output_records):
    with open(file_name, 'w') as f:
        write_header(f)
        for record in output_records:
            write_record(f, record)

# <codecell>

[record for record in class_records if record['index'] == '9122']

# <codecell>

def test_arguments(name, *keys):
    print(name)
    print(keys)
    print type(keys)

test_arguments('first', 'second', *('third', 'fourth'))

# <codecell>

normalized_class_data = [normalize_class_record(record) for record in final_map]
normalized_class_data = [record for record in normalized_class_data if filter_record_func(record)]
len(normalized_class_data)

