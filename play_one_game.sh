#!/usr/bin/env sh
tools/playgame.py --player_seed 42 --end_wait=0.25 --verbose --log_dir game_logs --turns 1000 --map_file tools/maps/random_walk/random_walk_04p_02.map "java -jar my-bot/MyBot.jar" "python tools/sample_bots/python/HunterBot.py" "java -jar tools/sample_bots/old.jar" "python tools/sample_bots/python/HunterBot.py" --verbose -e
