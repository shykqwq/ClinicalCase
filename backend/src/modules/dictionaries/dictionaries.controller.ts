import { Controller, Get } from '@nestjs/common';
import { DictionariesService } from './dictionaries.service';

@Controller('dictionaries')
export class DictionariesController {
  constructor(private readonly dictionariesService: DictionariesService) {}

  @Get()
  list() {
    return this.dictionariesService.list();
  }
}

