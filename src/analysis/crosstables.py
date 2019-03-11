#!/usr/bin/python3

import os
import re
import glob
import argparse
from collections import defaultdict


def parse_file(path):
    """
    Parses a result file and returns the count of victories of each player.
    :param path:
    :return: list with two numbers: the victories of each player
    """
    victory_count = [0, 0]  # victories for p1 and p2

    lines = open(path).readlines()[1:]

    # TODO use numpy rather than scan through the whole file manually
    for line in lines:
        winner = int(line.split(',')[0])
        if winner == 0:
            victory_count[0] += 1
        elif winner == 1:
            victory_count[1] += 1
    return victory_count


def results_per_map(inputdir):
    """
    Parses all result files from a directory, returning a dict.
    When used like: returned_dict[mapname][p1name][p2name], it gives
    the number of victores of p1 vs p2 in the given map
    :param inputdir:
    :return:
    """
    file_list = glob.glob(os.path.join(inputdir, '*.csv'))

    # usage: victories[mapname][player1][player2]
    victories = defaultdict(lambda: defaultdict(lambda: defaultdict(lambda: 0)))

    for path in file_list:
        filename = os.path.splitext(os.path.basename(path))[0]
        (p1, p2, mapname) = filename.split('_')

        result = parse_file(path)
        victories[mapname][p1][p2] += result[0]
        victories[mapname][p2][p1] += result[1]
        #print('%s - %s v %s: %d' % (mapname, p1, p2, result[0]))
        #print('%s - %s v %s: %d' % (mapname, p2, p1, result[1]))

    return victories


def results_per_map_size(inputdir):
    """
    Parses all result files from a directory, returning a dict.
    When used like: returned_dict[dimensions][p1name][p2name], it gives
    the number of victores of p1 vs p2 in all map of the given dimensions.
    The dimensions are a string in the form WxH (width vs height)
    :param inputdir:
    :return:
    """
    file_list = glob.glob(os.path.join(inputdir, '*.csv'))

    # usage: victories[dimension][player1][player2]
    victories = defaultdict(lambda: defaultdict(lambda: defaultdict(lambda: 0)))

    for path in file_list:
        filename = os.path.splitext(os.path.basename(path))[0]
        (p1, p2, mapname) = filename.split('_')
        dimensions = re.match(r"\D*(\d+x\d+)\w*", mapname).group(1)

        result = parse_file(path)
        victories[dimensions][p1][p2] += result[0]
        victories[dimensions][p2][p1] += result[1]

    return victories


def write_crosstable(results, outdir, view='percent'):
    """
    Receives the results, a 3d-dict, like results[mapname][p1name][p2name]
    and generates one file per mapname in outdir. The file is a .csv with
    a crosstable: the number of wins of the row player against the col. player
    :param results:
    :param outdir:
    :param view: 'percent' or 'raw' to give either the percent of victories or the raw number
    :return:
    """

    # TODO customize separator (comma, tab, etc)
    for mapname in results:
        f = open(os.path.join(outdir, 'results_%s.csv' % mapname), 'w')

        players = results[mapname]

        # writes the header
        f.write(','.join(['x'] + [player for player in players]) + '\n')

        view_func = win_percent if view == 'percent' else win_number

        # writes the cells (one row at a time)
        # a - is written if there's no record between a pair of players
        for p1 in players:
            f.write('%s\n' % ','.join([p1] + [view_func(results[mapname], p1, p2) for p2 in players]))

        f.close()


def win_number(data, p1, p2):
    """
    Returns a string with the number of victories of p1 versus p2 from a dict
    referenced in a 'tabular' form (data[p1][p2]). If data is absent, returns -
    :param data: a dict, where data[p1][p2] will give the number of victories of p1 vs p2
    :param p1:
    :param p2:
    :return:
    """
    return str(data[p1].get(p2, '-'))


def win_percent(data, p1, p2):
    """
    Returns a string with the win percent, with 2 decimal places, of p1 versus p2 from a
    dict with the number of victories
    :param data: a dict, where data[p1][p2] will give the number of victories of p1 vs p2
    :param p1:
    :param p2:
    :return: a string with the win percent or '-' if data is absent
    """
    if p2 not in data[p1] and p1 not in data[p2]:
        return '-'

    total = data[p1][p2] + data[p2][p1]

    return '%.2f' % (data[p1][p2] * 100.0 / total if total != 0 else 0)


if __name__ == '__main__':

    parser = argparse.ArgumentParser(
        description='Analyse the output of microRTS matches in the '
                    'PGSTest project and generates the crosstables'
    )

    parser.add_argument(
        'inputdir', help='Directory with the result files'
    )

    parser.add_argument(
        'outdir', help='Directory to generate the crosstables'
    )

    parser.add_argument(
        '-d', '--dimensions', action='store_true',
        help='Group by map dimensions rather than by the name'
    )

    parser.add_argument(
        '-v', '--view', choices=['percent', 'raw'], default='percent',
        help='Type of output to view: percent or raw number of victories.'
    )

    args = parser.parse_args()

    if args.dimensions:
        data = results_per_map_size(args.inputdir)
    else:
        data = results_per_map(args.inputdir)

    write_crosstable(data, args.outdir, args.view)

    print('DONE. Check the resulting files at %s' % args.outdir)
