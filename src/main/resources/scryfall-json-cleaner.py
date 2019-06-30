import json
import re

print("Reading list...")
with open("scryfall-default-cards.json", mode='r', encoding='utf-8') as fin:
    all_cards = json.load(fin)

    #Initial set of desirable cards
    clean_cards = [x for x in all_cards if x['border_color'] not in ['silver', 'gold'] \
                                        if x['oversized'] is False if x['digital'] is False if x['layout'] not in ["token", "emblem", "double_faced_token"] \
                                        if 'paper' in x['games'] if x['lang'] == 'en' if x['set_type'] not in ['masterpiece']]
    
    #Search for cards that only exist in promo form like "Nexus of Fate", "Impervious Greatwurm", etc.
    no_promos  = [x for x in clean_cards if x['promo'] is False]
    all_promos = [x for x in clean_cards if x['promo'] is True]

    for card in all_promos:
        if len([x for x in no_promos if x['oracle_id'] == card['oracle_id']]) == 0:
            no_promos.append(card)

    clean_cards = no_promos

    #Heroes of the Realm is a black-border un-set, have to handle specially
    clean_cards = [x for x in clean_cards if x['set'] != 'htr']

    #Generate tokens
    tokens = [x for x in all_cards if x['layout'] in ["token", "emblem", "double_faced_token"]]

    #List of keys to remove
    remove_keys = ['arena_id',
                   'artist',
                   'booster',
                   'border_color',
                   'card_back_id',
                   'cmc',
                   'color_identity',
                   'color_indicator',
                   'digital',
                   'edhrec_rank',
                   'flavor_text',
                   'foil',
                   'frame',
                   'frame_effect',
                   'full_art',
                   'games',
                   'highres_image', 
                   'illustration_id',
                   'lang', 
                   'layout',
                   'legalities', 
                   'loyalty', 
                   'mana_cost',
                   'mtgo_foil_id',
                   'mtgo_id',
                   'multiverse_ids',
                   'nonfoil',
                   'object',
                   'oversized',
                   'prints_search_uri',
                   'promo',
                   'promo_types',
                   'rarity', 
                   'related_uris',
                   'released_at',
                   'reprint',
                   'reserved',
                   'rulings_uri',
                   'scryfall_set_uri', 
                   'set_name',
                   'set_search_uri',
                   'set_type',
                   'set_uri', 
                   'story_spotlight',
                   'tcgplayer_id',
                   'textless',
                   'uri',
                   'variation',
                   'watermark']

    #Trim tokens
    for obj in tokens:
        for key in remove_keys:
            try:
                del obj[key]
            except:
                continue

    #Trim search
    out_cards = clean_cards
    remove_keys.append('type_line')
    for obj in out_cards: #tokens need a typeline
        for key in remove_keys:
            try:
                del obj[key]
            except:
                continue
    
    #Cut image sizes we don't use
    for size in ['png', 'art_crop', 'border_crop', 'large']:
        for card in out_cards:
            try:
                del card['image_uris'][size]
            except:
                continue
        for token in tokens:
            try:
                del token['image_uris'][size]
            except:
                continue
    
    #Write search
    with open("scryfall-clean.json", mode='w') as fout:
        json.dump(out_cards, fout)

    #Write tokens
    with open("scryfall-tokens.json", mode='w') as fout:
        json.dump(tokens, fout)

print("Done.")
