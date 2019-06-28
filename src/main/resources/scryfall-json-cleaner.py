import json
import re

print("Reading list...")
with open("scryfall-default-cards.json", mode='r', encoding='utf-8') as fin:
    all_cards = json.load(fin)

    #Initial set of desirable cards
    clean_cards = [x for x in all_cards if x['border_color'] not in ['silver', 'gold'] \
                                        if x['oversized'] is False if x['digital'] is False if x['layout'] not in ["token", "emblem"] \
                                        if 'paper' in x['games'] if x['lang'] == 'en']
    
    #Heroes of the Realm is a black-border un-set
    clean_cards = [x for x in clean_cards if x['set'] != 'htr']

    #Set control
    token_ids = set()
    for x in clean_cards:
        if x['oracle_id'] not in token_ids and 'all_parts' in x.keys():
            token_ids.add(x['oracle_id'])
    
    #Generate List
    """
    out_cards = []
    for id in token_ids:
        newlist = sorted([x for x in clean_cards if x['oracle_id'] == id if 'all_parts' in x.keys()], key=lambda x: len(x['all_parts']), reverse=True)
        out_cards.append(newlist[0])

    no_token_cards = [x for x in clean_cards if x['oracle_id'] not in token_ids if 'all_parts' not in x.keys() if not token_ids.add(x['oracle_id'])]
    """
    
    tokens = [x for x in all_cards if x['layout'] in ["token", "emblem", "double_faced_token"]]

    #List of keys to remove
    remove_keys = ('arena_id',
                   'artist',
                   'border_color',
                   'collector_number',
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
                   'rarity', 
                   'related_uris',
                   'released_at',
                   'reprint',
                   'reserved',
                   'rulings_uri',
                   'scryfall_set_uri', 
                   'set',
                   'set_name',
                   'set_search_uri',
                   'set_uri', 
                   'story_spotlight',
                   'tcgplayer_id',
                   'type_line',
                   'uri',
                   'watermark')

    #Write search
    remaining_keys = set()
    out_cards = clean_cards #out_cards + no_token_cards
    for obj in out_cards:
        for key in remove_keys:
            try:
                del obj[key]
            except:
                continue
    with open("scryfall-clean.json", mode='w') as fout:
        json.dump(out_cards, fout)

    #Write tokens
    for obj in tokens:
        for key in remove_keys:
            try:
                del obj[key]
            except:
                continue
    with open("scryfall-tokens.json", mode='w') as fout:
        json.dump(tokens, fout)

print("Done.")
