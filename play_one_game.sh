#!/usr/bin/env sh
tools/playgame.py --player_seed 42 --end_wait=0.25 --verbose --log_dir game_logs --turns 1000 --map_file tools/maps/maze/maze_04p_01.map "java -jar my-bot/MyBot.jar" "python tools/sample_bots/python/HunterBot.py" "python tools/sample_bots/python/LeftyBot.py" "python tools/sample_bots/python/HunterBot.py" --verbose -e
