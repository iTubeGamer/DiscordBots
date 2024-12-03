import discord
from discord import SelectOption, TextChannel, Interaction, app_commands
from discord.ui import Select, View
import logging
import os
import argparse
from typing import Optional
from dotenv import load_dotenv
from discord.ext import commands

def parse_arguments():
    parser = argparse.ArgumentParser(description='Discord Topic Channel Bot')
    parser.add_argument('--token', '-t',
                       default=os.environ.get('DISCORD_BOT_TOKEN'),
                       help='Discord bot token (can also be set via DISCORD_BOT_TOKEN environment variable)')
    parser.add_argument('--log-path', '-l',
                       default='/home/max/serious-gaming-bot/log/serious-gaming-bot.log',
                       help='Path to log file')
    return parser.parse_args()

class TopicChannelSelect(Select):
    def __init__(self, channels: list[TextChannel], member: discord.Member):
        sorted_channels = sorted(channels, key=lambda c: c.position)
        options = [
            SelectOption(
                label=channel.name,
                value=str(channel.id),
                default=channel.permissions_for(member).view_channel
            )
            for channel in sorted_channels
        ]
        
        super().__init__(
            placeholder="Select channels to show...",
            min_values=0,
            max_values=len(options),
            options=options
        )

    async def callback(self, interaction: Interaction):
        await interaction.response.defer(ephemeral=True)
        member = interaction.user
        
        category = interaction.guild.get_channel(int(self.options[0].value)).category
        topic_channels = category.text_channels
        
        for channel in topic_channels:
            should_see = str(channel.id) in self.values
            await channel.set_permissions(member, view_channel=should_see)
        
        view = ChannelManagerView(topic_channels, member)
        await interaction.edit_original_response(view=view)

class ChannelManagerView(View):
    def __init__(self, channels: list[TextChannel], member: discord.Member):
        super().__init__(timeout=None)
        self.add_item(TopicChannelSelect(channels, member))

class TopicChannelBot(commands.Bot):  # Changed to inherit from commands.Bot
    def __init__(self):
        intents = discord.Intents.default()
        intents.message_content = True
        intents.guilds = True
        intents.members = True
        
        super().__init__(command_prefix='/', intents=intents)

    async def setup_hook(self):
        """Called when the bot starts up"""
        logger.info("Starting to sync commands...")
        await self.register_commands()

    async def register_commands(self):
        @self.tree.command(name="channels", description="Open the channel selection menu")
        async def channels(interaction: Interaction):
            category = discord.utils.get(interaction.guild.categories, name="Topic Channels")
            if not category:
                await interaction.response.send_message("No Topic Channels category found!", ephemeral=True)
                return
                
            topic_channels = category.text_channels
            if not topic_channels:
                await interaction.response.send_message("No topic channels found!", ephemeral=True)
                return
            
            view = ChannelManagerView(topic_channels, interaction.user)
            await interaction.response.send_message(
                "**Topic Channel Manager**\nSelect which channels you want to see:",
                view=view,
                ephemeral=True
            )

        # Sync commands
        try:
            logger.info(f"Bot is in {len(self.guilds)} guilds: {[guild.name for guild in self.guilds]}")
            commands = await self.tree.sync()
            logger.info(f"Synced {len(commands)} commands globally: {[cmd.name for cmd in commands]}")
        except Exception as e:
            logger.error(f"Error during command sync: {str(e)}", exc_info=True)

    async def on_ready(self):
        logger.info(f'Bot is ready and logged in as {self.user}!')

def main():
    load_dotenv()
    args = parse_arguments()
    
    # Set up logging
    logging.basicConfig(
        level=logging.INFO,
        format='%(asctime)s - %(name)s - %(levelname)s - %(message)s',
        handlers=[
            logging.FileHandler(filename=args.log_path, encoding='utf-8', mode='w'),
            logging.StreamHandler()
        ]
    )
    global logger
    logger = logging.getLogger('serious-gaming-bot')

    try:
        bot = TopicChannelBot()
        bot.run(args.token)
    except Exception as e:
        logger.critical(f'Failed to start bot: {str(e)}', exc_info=True)

if __name__ == "__main__":
    main()