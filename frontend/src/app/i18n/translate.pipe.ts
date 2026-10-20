import { Pipe, PipeTransform } from '@angular/core';
import en from '../../assets/i18n/en.json';
import hi from '../../assets/i18n/hi.json';

type TranslationMap = Record<string, string>;

@Pipe({ name: 'translate', standalone: true, pure: true })
export class TranslatePipe implements PipeTransform {
  transform(key: string, language: 'en' | 'hi' = 'en'): string {
    const dictionary = (language === 'hi' ? hi : en) as TranslationMap;
    return dictionary[key] ?? key;
  }
}
