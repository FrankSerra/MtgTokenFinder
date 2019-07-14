import json
import re

print("Reading list...")
with open("scryfall-default-cards.json", mode='r', encoding='utf-8') as fin:
    all_cards = json.load(fin)

    #Initial set of desirable cards
    clean_cards = [x for x in all_cards if x['border_color'] not in ['gold'] \
                                        if x['oversized'] is False if x['digital'] is False if x['layout'] not in ["token", "emblem", "double_faced_token"] \
                                        if 'paper' in x['games'] if x['lang'] == 'en' if x['set_type'] not in ['masterpiece', 'memorabilia']]
    
    #Search for cards that only exist in promo form like "Nexus of Fate", "Impervious Greatwurm", etc.
    no_promos  = [x for x in clean_cards if x['promo'] is False]
    all_promos = [x for x in clean_cards if x['promo'] is True]

    for card in all_promos:
        if len([x for x in no_promos if x['oracle_id'] == card['oracle_id']]) == 0:
            no_promos.append(card)

    clean_cards = no_promos

    #Heroes of the Realm is a black-bordered un-set, have to handle manually
    clean_cards = [x for x in clean_cards if x['set'] != 'htr']

    #Generate tokens
    tokens = [x for x in all_cards if x['layout'] in ["token", "emblem", "double_faced_token"]]

    #List of keys to remove
    remove_keys = ['arena_id',
                   'artist',
                   'booster',
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
                   'textless',
                   'uri',
                   'variation',
                   'watermark']

    #Trim tokens
    for obj in tokens:
        for key in remove_keys:
            try:
                del obj[key]
                for face in obj['card_faces']:
                    del face[key]
            except:
                continue

    #Trim search
    out_cards = clean_cards
    remove_keys.extend(['type_line', 'colors']) #tokens need a typeline and colors, so we only remove it from the actual cards
    for obj in out_cards: 
        for key in remove_keys:
            try:
                del obj[key]
                for face in obj['card_faces']:
                    del face[key]
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

    #Generate lists of "contains create" vs "other" cards - "others" only need their names written
    silver_out_cards = [x for x in out_cards if x['border_color'] in ['silver']]
    out_cards = [x for x in out_cards if x['border_color'] not in ['silver']]

    out_cards_creators = []
    out_cards_other = []
    for card in out_cards:
        found = False
        if 'oracle_text' in card.keys():
            if 'create' in card['oracle_text'].lower() or 'emblem' in card['oracle_text'].lower():
                out_cards_creators.append(card)
                found = True
        elif 'card_faces' in card.keys():
            for face in card['card_faces']:
                if 'oracle_text' in face.keys():
                    if 'create' in face['oracle_text'].lower() or 'emblem' in face['oracle_text'].lower():
                        out_cards_creators.append(card)
                        found = True
                        break
        if found is False:
            for key in ['scryfall_uri', 'image_uris', 'oracle_text', 'power', 'toughness', 'set']:
                try:
                    del card[key]
                except:
                    continue
            out_cards_other.append(card)

    silver_out_cards_creators = []
    silver_out_cards_other = []
    for card in silver_out_cards:
        found = False
        if 'oracle_text' in card.keys():
            if 'create' in card['oracle_text'].lower() or 'emblem' in card['oracle_text'].lower():
                silver_out_cards_creators.append(card)
                found = True
        elif 'card_faces' in card.keys():
            for face in card['card_faces']:
                if 'oracle_text' in face.keys():
                    if 'create' in face['oracle_text'].lower() or 'emblem' in face['oracle_text'].lower():
                        silver_out_cards_creators.append(card)
                        found = True
                        break
        if found is False:
            for key in ['scryfall_uri', 'image_uris', 'oracle_text', 'power', 'toughness', 'set']:
                try:
                    del card[key]
                except:
                    continue
            silver_out_cards_other.append(card)

    with open("scryfall-clean.json", mode='w') as fout:
        out_cards_creators.extend(out_cards_other)
        json.dump(out_cards_creators, fout)

    #Write silver-bordered cards
    with open("scryfall-silver.json", mode='w') as fout:
        silver_out_cards_creators.extend(silver_out_cards_other)
        json.dump(silver_out_cards_creators, fout)

    #Write tokens
    with open("scryfall-tokens.json", mode='w') as fout:
        json.dump(tokens, fout)

print("Done.")